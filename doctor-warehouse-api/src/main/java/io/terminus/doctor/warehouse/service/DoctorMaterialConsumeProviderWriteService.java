package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeDto;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 添加事件信息录入
 */
public interface DoctorMaterialConsumeProviderWriteService {

    /**
     * 添加原料消耗事件信息
     * @param doctorMaterialConsumeDto
     * @return
     */
    Response<Boolean> createMaterialConsumerEvent(DoctorMaterialConsumeDto doctorMaterialConsumeDto);

}
