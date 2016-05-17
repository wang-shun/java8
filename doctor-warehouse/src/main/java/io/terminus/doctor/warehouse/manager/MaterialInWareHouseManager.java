package io.terminus.doctor.warehouse.manager;

import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeDto;
import lombok.extern.slf4j.Slf4j;
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
//        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider = DoctorMaterialConsumeDto

        //录入计算 对应数量信息

        //计算平均消耗信息

        //更新的猪场WareHouse

        //更新猪场整体的数量信息

        return Boolean.FALSE;
    }
}
