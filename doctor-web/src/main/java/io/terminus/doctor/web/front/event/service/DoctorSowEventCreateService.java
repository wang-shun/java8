package io.terminus.doctor.web.front.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorSowEventCreateService {

    /**
     * 母猪事件信息创建
     * @param doctorBasicInputInfoDto
     * @param pigEvent
     * @param params
     * @return
     */
    Response<Long> sowEventCreate(DoctorBasicInputInfoDto doctorBasicInputInfoDto, Map<String, Object> params);

}
