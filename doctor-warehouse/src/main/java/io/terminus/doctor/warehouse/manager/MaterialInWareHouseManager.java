package io.terminus.doctor.warehouse.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.NumberUtils;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.dto.EventHandlerContext;
import io.terminus.doctor.warehouse.handler.DoctorWareHouseHandlerInvocation;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe: 物料消耗 manager 信息管理方式
 */
@Slf4j
@Component
public class MaterialInWareHouseManager {

    private final DoctorWareHouseHandlerInvocation doctorWareHouseHandlerInvocation;

    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    private final DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public MaterialInWareHouseManager(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,
                                      DoctorMaterialInfoDao doctorMaterialInfoDao,
                                      DoctorWareHouseHandlerInvocation doctorWareHouseHandlerInvocation,
                                      DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao,
                                      DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
        this.doctorWareHouseHandlerInvocation = doctorWareHouseHandlerInvocation;
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    // 生产对应的物料内容
    @Transactional
    public Boolean produceMaterialInfo(DoctorWareHouseBasicDto basicDto,
                                       DoctorWareHouse targetHouse, DoctorMaterialInfo targetMaterial,
                                       DoctorMaterialInfo.MaterialProduce materialProduce){
        List<Long> eventIds = new ArrayList<>();

        // consume each source
        materialProduce.getMaterialProduceEntries().forEach(m->
                eventIds.addAll(produceMaterialConsumeEntry(basicDto, m))
        );

        materialProduce.getMedicalProduceEntries().forEach(m->
                eventIds.addAll(produceMaterialConsumeEntry(basicDto, m))
        );

        // 计算本次配方生产的饲料的入库单价
        Long unitPrice = this.calculateProviderUnitPrice(eventIds, materialProduce.getTotal());
        // provider source
        providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto.builder()
                .type(targetMaterial.getType()).farmId(targetMaterial.getFarmId()).farmName(targetMaterial.getFarmName())
                .materialTypeId(targetMaterial.getId()).materialName(targetMaterial.getMaterialName())
                .wareHouseId(targetHouse.getId()).wareHouseName(targetHouse.getWareHouseName())
                .barnId(basicDto.getBarnId()).barnName(basicDto.getBarnName()).staffId(basicDto.getStaffId()).staffName(basicDto.getStaffName())
                .count(materialProduce.getTotal()).unitId(targetMaterial.getUnitId()).unitName(targetMaterial.getUnitName()).unitPrice(unitPrice)
                .build());

        return Boolean.TRUE;
    }

    private Long calculateProviderUnitPrice(List<Long> eventIds, Double realTotal){
        long totalPrice = 0L;
        for (DoctorMaterialConsumeProvider cp : doctorMaterialConsumeProviderDao.findByIds(eventIds)){
            Map<String, Object> extraMap = cp.getExtraMap();
            ArrayList<Map<String, Object>> array = (ArrayList) extraMap.get("consumePrice");
            for(Map<String, Object> obj : array){
                double count = Double.parseDouble(obj.get("count").toString());
                long unitPrice = Long.parseLong(obj.get("unitPrice").toString());
                totalPrice += count * unitPrice;
            }
        }
        return Long.valueOf(NumberUtils.divide(totalPrice, realTotal.longValue(), 0));
    }

    private List<Long> produceMaterialConsumeEntry(DoctorWareHouseBasicDto basicDto, DoctorMaterialInfo.MaterialProduceEntry materialProduceEntry){
        List<Long> eventIds = new ArrayList<>();
        List<DoctorMaterialInWareHouse> doctorMaterialInWareHouses = doctorMaterialInWareHouseDao.queryByFarmMaterial(
                basicDto.getFarmId(),
                materialProduceEntry.getMaterialId());

        double totalCount = doctorMaterialInWareHouses.stream().map(DoctorMaterialInWareHouse::getLotNumber).reduce((d1, d2) -> d1 + d2).orElse(0D);
        checkState(totalCount >= materialProduceEntry.getMaterialCount(), "not.enough.source");


        // consume
        double totalConsumeCount= materialProduceEntry.getMaterialCount();
        int index = 0;

        while (totalConsumeCount!=0){
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouses.get(index);
            index+=1;
            double toConsume = doctorMaterialInWareHouse.getLotNumber();

            if(toConsume>=totalConsumeCount){
                toConsume = totalConsumeCount;
                totalConsumeCount = 0;
            }else {
                totalConsumeCount = totalConsumeCount - toConsume;
            }

            Long eventId = consumeMaterialInner(DoctorMaterialConsumeProviderDto.builder()
                    .type(doctorMaterialInWareHouse.getType())
                    .farmId(doctorMaterialInWareHouse.getFarmId()).farmName(doctorMaterialInWareHouse.getFarmName())
                    .materialTypeId(doctorMaterialInWareHouse.getMaterialId()).materialName(doctorMaterialInWareHouse.getMaterialName())
                    .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                    .barnId(basicDto.getBarnId()).barnName(basicDto.getBarnName()).staffId(basicDto.getStaffId()).staffName(basicDto.getStaffName())
                    .count(toConsume).unitName(doctorMaterialInWareHouse.getUnitName())
                    .build());
            eventIds.add(eventId);
        }
        return eventIds;
    }

    /**
     * 用户消耗信息录入内容
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    @Transactional
    public Long consumeMaterial(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        return consumeMaterialInner(doctorMaterialConsumeProviderDto);
    }

    @Transactional
    public void moveMaterial(DoctorMaterialConsumeProviderDto diaochuDto, DoctorMaterialConsumeProviderDto diaoruDto){
        // 先调出
        Long diaochuEventId = consumeMaterialInner(diaochuDto);
        DoctorMaterialConsumeProvider diaochuEvent = doctorMaterialConsumeProviderDao.findById(diaochuEventId);

        // 然后调入
        diaoruDto.setUnitPrice(diaochuEvent.getUnitPrice());
        Long diaoruEventId = providerMaterialInWareHouseInner(diaoruDto);

        // 把两个事件的 id 在对方的 extra 中记录一下
        Map<String, Object> diaochuMap = diaochuEvent.getExtraMap();
        if(diaochuMap == null){
            diaochuMap = new HashMap<>();
        }
        diaochuMap.put("relEventId", diaoruEventId);
        diaochuEvent.setExtraMap(diaochuMap);
        doctorMaterialConsumeProviderDao.update(diaochuEvent);

        DoctorMaterialConsumeProvider diaoruEvent = doctorMaterialConsumeProviderDao.findById(diaoruEventId);
        Map<String, Object> diaoruMap = diaoruEvent.getExtraMap();
        if(diaoruMap == null){
            diaoruMap = new HashMap<>();
        }
        diaoruMap.put("relEventId", diaochuEventId);
        diaoruEvent.setExtraMap(diaoruMap);
        doctorMaterialConsumeProviderDao.update(diaoruEvent);
    }

    /**
     * 用户信息的提供操作
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    @Transactional
    public Long providerMaterialInWareHouse(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        return providerMaterialInWareHouseInner(doctorMaterialConsumeProviderDto);
    }

    /**
     * 删除对应的物料信息
     * @param materialInWareHouseId
     * @return
     */
    @Transactional
    public Boolean deleteMaterialInWareHouse(Long materialInWareHouseId, Long userId, String userName){
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.findById(materialInWareHouseId);
        checkState(!isNull(doctorMaterialInWareHouse), "input.materialInWareHouseId.empty");

        DoctorMaterialInfo doctorMaterialInfo = doctorMaterialInfoDao.findById(doctorMaterialInWareHouse.getMaterialId());
        checkState(!isNull(doctorMaterialInfo), "query.materialInfo.fail");
        Integer consumeDays = (int)(doctorMaterialInWareHouse.getLotNumber() / doctorMaterialInfo.getDefaultConsumeCount());
        if (consumeDays == 0 ) consumeDays = 1; // 修改1

        // 消耗对应的物资信息
        consumeMaterialInner(DoctorMaterialConsumeProviderDto.builder().type(doctorMaterialInWareHouse.getType())
                .farmId(doctorMaterialInWareHouse.getFarmId()).farmName(doctorMaterialInWareHouse.getFarmName())
                .materialTypeId(doctorMaterialInWareHouse.getMaterialId()).materialName(doctorMaterialInWareHouse.getMaterialName())
                .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                .barnId(0l).barnName("")   //默认消耗仓库信息
                .staffId(userId).staffName(userName).count(doctorMaterialInWareHouse.getLotNumber())
                .unitId(doctorMaterialInfo.getUnitId()).unitName(userName).consumeDays(consumeDays)
                .build());

        // delete in warehouse
        doctorMaterialInWareHouseDao.delete(materialInWareHouseId);

        // delete consum avg
        DoctorMaterialConsumeAvg doctorMaterialConsumeAvg = doctorMaterialConsumeAvgDao.queryByIds(
                doctorMaterialInWareHouse.getFarmId(), doctorMaterialInWareHouse.getWareHouseId(), doctorMaterialInWareHouse.getMaterialId());
        checkState(!isNull(doctorMaterialConsumeAvg), "query.materialInWareHouse.empty");
        doctorMaterialConsumeAvgDao.delete(doctorMaterialConsumeAvg.getId());
        return Boolean.TRUE;
    }

    private Long providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto dto){
        if(dto.getActionType() == null){
            dto.setActionType(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue());
        }
        EventHandlerContext context = new EventHandlerContext();
        doctorWareHouseHandlerInvocation.invoke(dto, context);
        return context.getEventId();
    }

    private Long consumeMaterialInner(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        if(doctorMaterialConsumeProviderDto.getActionType() == null){
            doctorMaterialConsumeProviderDto.setActionType(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
        }
        EventHandlerContext context = new EventHandlerContext();
        this.doctorWareHouseHandlerInvocation.invoke(doctorMaterialConsumeProviderDto, context);
        return context.getEventId();
    }

    @Transactional
    public void rollback(Long eventId){
        DoctorMaterialConsumeProvider cp = doctorMaterialConsumeProviderDao.findById(eventId);
        if(cp == null){
            throw new ServiceException("event.not.found");
        }

        // 对调拨事件要特别对待, 因为有一个关联事件也要一起回滚
        if(Objects.equals(cp.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.DIAORU.getValue())
                || Objects.equals(cp.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU.getValue())){
            Long relEventId;
            try {
                relEventId = Long.valueOf(cp.getExtraMap().get("relEventId").toString());
            } catch (RuntimeException e) {
                throw new ServiceException("related.event.not.fount"); // 没有找到关联事件, 无法回滚
            }
            DoctorMaterialConsumeProvider relEvent = doctorMaterialConsumeProviderDao.findById(relEventId);
            if(relEvent == null){
                throw new ServiceException("related.event.not.fount");
            }
            doctorWareHouseHandlerInvocation.rollback(relEvent);
        }
        doctorWareHouseHandlerInvocation.rollback(cp);
    }
}
