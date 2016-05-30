package io.terminus.doctor.warehouse.manager;

import com.google.common.collect.Maps;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.handler.DoctorWareHouseHandlerInvocation;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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

    @Autowired
    public MaterialInWareHouseManager(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,
                                      DoctorMaterialInfoDao doctorMaterialInfoDao,
                                      DoctorWareHouseHandlerInvocation doctorWareHouseHandlerInvocation){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
        this.doctorWareHouseHandlerInvocation = doctorWareHouseHandlerInvocation;
    }

    // 生产对应的物料内容
    @Transactional
    public Boolean produceMaterialInfo(DoctorWareHouseBasicDto basicDto,
                                       DoctorWareHouse targetHouse, DoctorMaterialInfo targetMaterial,
                                       DoctorMaterialInfo.MaterialProduce materialProduce){

        // consume each source
        materialProduce.getMaterialProduceEntries().forEach(m->
            produceMaterialConsumeEntry(basicDto, m));

        materialProduce.getMedicalProduceEntries().forEach(m->
            produceMaterialConsumeEntry(basicDto, m));

        // provider source
        providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto.builder()
                .type(targetMaterial.getType()).farmId(targetMaterial.getFarmId()).farmName(targetMaterial.getFarmName())
                .materialTypeId(targetMaterial.getId()).materialName(targetMaterial.getMaterialName())
                .wareHouseId(targetHouse.getId()).wareHouseName(targetHouse.getWareHouseName())
                .barnId(basicDto.getBarnId()).barnName(basicDto.getBarnName()).staffId(basicDto.getStaffId()).staffName(basicDto.getStaffName())
                .count(materialProduce.getTotal()).unitId(targetMaterial.getUnitId()).unitName(targetMaterial.getUnitName())
                .build());

        return Boolean.TRUE;
    }

    private void produceMaterialConsumeEntry(DoctorWareHouseBasicDto basicDto, DoctorMaterialInfo.MaterialProduceEntry materialProduceEntry){
        // get material
        DoctorMaterialInfo materialInfo = doctorMaterialInfoDao.findById(materialProduceEntry.getMaterialId());
        checkState(!isNull(materialInfo), "produce.sourceMaterial.empty");

        List<DoctorMaterialInWareHouse> doctorMaterialInWareHouses = doctorMaterialInWareHouseDao.queryByFarmMaterial(basicDto.getFarmId(), materialInfo.getId());

        long totalCount = doctorMaterialInWareHouses.stream().map(DoctorMaterialInWareHouse::getLotNumber).reduce(Math::addExact).orElse(0l);
        checkState(totalCount >= materialProduceEntry.getMaterialCount(), "not.enough.source");


        // consume
        long totalConsumeCount= materialProduceEntry.getMaterialCount();
        int index = 0;

        while (totalConsumeCount!=0){
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouses.get(index);
            index+=1;
            long toConsume = doctorMaterialInWareHouse.getLotNumber();

            if(toConsume>=totalConsumeCount){
                toConsume = totalConsumeCount;
                totalConsumeCount = 0;
            }else {
                totalConsumeCount = totalConsumeCount - toConsume;
            }

            consumeMaterialInner(DoctorMaterialConsumeProviderDto.builder()
                    .type(materialInfo.getType()).farmId(materialInfo.getFarmId()).farmName(materialInfo.getFarmName())
                    .materialTypeId(materialInfo.getId()).materialName(materialInfo.getMaterialName())
                    .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                    .barnId(basicDto.getBarnId()).barnName(basicDto.getBarnName()).staffId(basicDto.getStaffId()).staffName(basicDto.getStaffName())
                    .count(toConsume).unitId(materialInfo.getUnitId()).unitName(materialInfo.getUnitName())
                    .build());
        }

        return;
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
     * 用户信息的提供操作
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    @Transactional
    public Long providerMaterialInWareHouse(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        return providerMaterialInWareHouseInner(doctorMaterialConsumeProviderDto);
    }

    private Long providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto dto){
        Map<String,Object> context = Maps.newHashMap();
        doctorWareHouseHandlerInvocation.invoke(dto, context);
        return Long.valueOf(context.get("eventId").toString());
    }

    private Long consumeMaterialInner(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        Map<String,Object> context = Maps.newHashMap();
        this.doctorWareHouseHandlerInvocation.invoke(doctorMaterialConsumeProviderDto, context);
        return Long.valueOf(context.get("eventId").toString());
    }

}
