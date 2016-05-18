package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;

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
    Response<Boolean> consumeMaterialInfo(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto);

}
