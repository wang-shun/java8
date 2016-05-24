package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.manager.MaterialInWareHouseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public DoctorMaterialInWareHouseWriteServiceImpl(MaterialInWareHouseManager materialInWareHouseManager){
        this.materialInWareHouseManager = materialInWareHouseManager;
    }

    @Override
    public Response<Boolean> consumeMaterialInfo(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto) {
        try{

            // TODO validate field parameter info

            return Response.ok(materialInWareHouseManager.consumeMaterialInWareHouse(doctorMaterialConsumeProviderDto));
        }catch (Exception e){
            log.error("consumer material info error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("consume.material.error");
        }
    }

    public Response<Boolean> providerMaterialInfo(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        try{

            // TODO validate field param info

            return Response.ok(materialInWareHouseManager.providerMaterialInWareHouse(doctorMaterialConsumeProviderDto));
        }catch (Exception e){
            log.error("provider material info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("provider.materialInfo.fail");
        }
    }
}
