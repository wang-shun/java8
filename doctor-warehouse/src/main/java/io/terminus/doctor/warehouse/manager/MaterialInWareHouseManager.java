package io.terminus.doctor.warehouse.manager;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe: 物料消耗 manager 信息管理方式
 */
@Slf4j
@Component
public class MaterialInWareHouseManager {

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    public MaterialInWareHouseManager(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    /**
     * 用户消耗信息录入内容
     * @param doctorMaterialConsumeDto
     * @return
     */
    @Transactional
    public Boolean consumeMaterialInWareHouse(DoctorMaterialConsumeDto doctorMaterialConsumeDto){

        //录入事件信息
        doctorMaterialConsumeProviderDao.create(builderMaterialEvent(doctorMaterialConsumeDto));

        //录入计算 对应数量信息

        //计算平均消耗信息

        //更新的猪场WareHouse

        //更新猪场整体的数量信息

        return Boolean.FALSE;
    }


    /**
     * 构造用户消耗事件
     * @param doctorMaterialConsumeDto
     * @return
     */
    private DoctorMaterialConsumeProvider builderMaterialEvent(DoctorMaterialConsumeDto doctorMaterialConsumeDto){
        DoctorMaterialConsumeProvider result = DoctorMaterialConsumeProvider.builder()
                .type(doctorMaterialConsumeDto.getType())
                .farmId(doctorMaterialConsumeDto.getFarmId()).farmName(doctorMaterialConsumeDto.getFarmName())
                .wareHouseId(doctorMaterialConsumeDto.getWareHouseId()).wareHouseName(doctorMaterialConsumeDto.getWareHouseName())
                .materialId(doctorMaterialConsumeDto.getMaterialTypeId()).materialName(doctorMaterialConsumeDto.getMaterialName())
                .eventType(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue()).eventTime(DateTime.now().toDate()).eventCount(doctorMaterialConsumeDto.getConsumeCount())
                .staffId(doctorMaterialConsumeDto.getStaffId()).staffName(doctorMaterialConsumeDto.getStaffName())
                .creatorId(doctorMaterialConsumeDto.getStaffId()).creatorName(doctorMaterialConsumeDto.getStaffName())
                .createdAt(DateTime.now().toDate())
                .build();

        // validate feed 类型， 统计消耗天数
        result.setExtraMap(ImmutableMap.of("consumeDays",doctorMaterialConsumeDto.getConsumeDays()));

        return result;
    }
}
