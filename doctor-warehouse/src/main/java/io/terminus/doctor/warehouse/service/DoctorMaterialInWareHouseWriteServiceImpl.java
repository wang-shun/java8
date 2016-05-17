package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeDto;
import io.terminus.doctor.warehouse.manager.MaterialInWareHouseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
public class DoctorMaterialInWareHouseWriteServiceImpl implements DoctorMaterialInWareHouseWriteService{

    private final MaterialInWareHouseManager materialInWareHouseManager;

    public DoctorMaterialInWareHouseWriteServiceImpl(MaterialInWareHouseManager materialInWareHouseManager){
        this.materialInWareHouseManager = materialInWareHouseManager;
    }

    @Override
    public Response<Boolean> consumeMaterialInfo(DoctorMaterialConsumeDto doctorMaterialConsumeDto) {
        try{

            // TODO validate field parameter info

            return Response.ok(materialInWareHouseManager.consumeMaterialInWareHouse(doctorMaterialConsumeDto));
        }catch (Exception e){
            log.error("consumer material info error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("consume.material.error");
        }
    }
}
