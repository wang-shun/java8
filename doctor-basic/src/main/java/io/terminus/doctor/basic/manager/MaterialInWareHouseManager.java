package io.terminus.doctor.basic.manager;

import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.NumberUtils;
import io.terminus.doctor.basic.dto.DoctorMoveMaterialDto;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.basic.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.basic.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.basic.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.handler.DoctorWareHouseHandlerInvocation;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.FeedFormula;
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

    private final DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public MaterialInWareHouseManager(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,
                                      DoctorWareHouseHandlerInvocation doctorWareHouseHandlerInvocation,
                                      DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao,
                                      DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorWareHouseHandlerInvocation = doctorWareHouseHandlerInvocation;
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    // 生产对应的物料内容
    @Transactional
    public Boolean produceMaterialInfo(DoctorWareHouseBasicDto basicDto, DoctorWareHouse targetHouse, FeedFormula feedFormula,
                                       Long feedUnitId, String feedUnitName, FeedFormula.FeedProduce materialProduce){
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
        Long providerEventId = providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto.builder()
                .type(WareHouseType.FEED.getKey()).farmId(feedFormula.getFarmId()).farmName(feedFormula.getFarmName())
                .materialTypeId(feedFormula.getFeedId()).materialName(feedFormula.getFeedName())
                .wareHouseId(targetHouse.getId()).wareHouseName(targetHouse.getWareHouseName())
                .barnId(basicDto.getBarnId()).barnName(basicDto.getBarnName()).staffId(basicDto.getStaffId()).staffName(basicDto.getStaffName())
                .count(materialProduce.getTotal()).unitId(feedUnitId).unitName(feedUnitName).unitPrice(unitPrice)
                .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_FEED.getValue())
                .build());

        // 记录关联id, 为了以后的回滚使用
        eventIds.add(providerEventId);
        for(Long id : eventIds){
            DoctorMaterialConsumeProvider cp = doctorMaterialConsumeProviderDao.findById(id);
            Map<String, Object> extraMap = cp.getExtraMap() == null ? new HashMap<>() : cp.getExtraMap();
            List<Long> copy = Lists.newArrayList(eventIds);
            copy.remove(id);
            extraMap.put("relEventIds", copy);
            cp.setExtraMap(extraMap);
            doctorMaterialConsumeProviderDao.update(cp);
        }

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

    private List<Long> produceMaterialConsumeEntry(DoctorWareHouseBasicDto basicDto, FeedFormula.MaterialProduceEntry materialProduceEntry){
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
                    .type(doctorMaterialInWareHouse.getType()).actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_RAW_MATERIAL.getValue())
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

    /**
     * 批量出库
     * @param doctorMaterialConsumeProviderDtoList
     */
    @Transactional
    public void batchConsumeMaterial(List<DoctorMaterialConsumeProviderDto> doctorMaterialConsumeProviderDtoList){
        doctorMaterialConsumeProviderDtoList.forEach(this::consumeMaterialInner);
    }

    /**
     * 调拨
     * @param dto
     */
    @Transactional
    public void moveMaterial(DoctorMoveMaterialDto dto){
        moveMaterialImpl(dto);
    }

    /**
     * 批量调拨
     *
     * @param dtoList
     */
    @Transactional
    public void batchMoveMaterial(List<DoctorMoveMaterialDto> dtoList) {
        dtoList.forEach(this::moveMaterialImpl);
    }

    /**
     * 批量盘点
     * @param dtoList
     */
    @Transactional
    public void batchInventory(List<DoctorMaterialConsumeProviderDto> dtoList) {
        dtoList.forEach(dto -> {
            if (Objects.equals(dto.getActionType(), DoctorMaterialConsumeProvider.EVENT_TYPE.PANYING.getValue())) {
                providerMaterialInWareHouseInner(dto);
            } else if (Objects.equals(dto.getActionType(), DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI.getValue())) {
                consumeMaterialInner(dto);
            }
        });
    }
    /**
     * 调拨的具体实现
     * @param dto
     */
    private void moveMaterialImpl(DoctorMoveMaterialDto dto) {
        DoctorMaterialConsumeProviderDto diaochuDto = dto.getDiaochuDto();
        DoctorMaterialConsumeProviderDto diaoruDto = dto.getDiaoruDto();
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
     * 批量入库
     * @param doctorMaterialConsumeProviderDtoList
     * @return
     */
    @Transactional
    public void batchProviderMaterialInWareHouse(List<DoctorMaterialConsumeProviderDto> doctorMaterialConsumeProviderDtoList){
        doctorMaterialConsumeProviderDtoList.forEach(this::providerMaterialInWareHouseInner);
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

        // 消耗对应的物资信息
        consumeMaterialInner(DoctorMaterialConsumeProviderDto.builder().type(doctorMaterialInWareHouse.getType())
                .farmId(doctorMaterialInWareHouse.getFarmId()).farmName(doctorMaterialInWareHouse.getFarmName())
                .materialTypeId(doctorMaterialInWareHouse.getMaterialId()).materialName(doctorMaterialInWareHouse.getMaterialName())
                .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                .staffId(userId).staffName(userName).count(doctorMaterialInWareHouse.getLotNumber())
                .unitName(doctorMaterialInWareHouse.getUnitName()).consumeDays(1)
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
        EventHandlerContext context = new EventHandlerContext();
        doctorWareHouseHandlerInvocation.invoke(dto, context);
        return context.getEventId();
    }

    private Long consumeMaterialInner(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
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
        // 配方生产事件有更多的关联事件, 他妈的要找出来一起回滚
        if(Objects.equals(cp.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_FEED.getValue())
                || Objects.equals(cp.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_RAW_MATERIAL.getValue())){
            List<Long> relEventIds;
            try {
                relEventIds = JsonMapper.nonDefaultMapper().fromJson(cp.getExtraMap().get("relEventIds").toString(),
                        JsonMapper.nonDefaultMapper().createCollectionType(ArrayList.class, Long.class));
            } catch (RuntimeException e) {
                throw new ServiceException("related.event.not.fount"); // 没有找到关联事件, 无法回滚
            }
            relEventIds.forEach(id -> {
                DoctorMaterialConsumeProvider relEvent = doctorMaterialConsumeProviderDao.findById(id);
                if(relEvent == null){
                    throw new ServiceException("related.event.not.fount");
                }
                doctorWareHouseHandlerInvocation.rollback(relEvent);
            });
        }
        doctorWareHouseHandlerInvocation.rollback(cp);
    }
}
