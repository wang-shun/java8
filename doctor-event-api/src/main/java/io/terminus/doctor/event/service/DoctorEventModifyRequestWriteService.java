package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;

/**
 * Created by xjn on 17/3/10.
 * 编辑事件写service
 */
public interface DoctorEventModifyRequestWriteService {

    /**
     * 创建事件编辑请求
     *@param modifyRequest 事件编辑请求
     * @return 是否编辑成功
     */
    Response<Boolean> createRequest(DoctorEventModifyRequest modifyRequest);
}
