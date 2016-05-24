package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;

import javax.validation.constraints.NotNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorMaterialInWareHouseWriteService {

    /**
     * 录入用户消耗信息
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    Response<Boolean> consumeMaterialInfo(@NotNull(message = "input.dto.empty") DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto);

    /**
     * 用户录入生产数量信息
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    Response<Boolean> providerMaterialInfo(@NotNull(message = "input.dto.empty") DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto);
}
