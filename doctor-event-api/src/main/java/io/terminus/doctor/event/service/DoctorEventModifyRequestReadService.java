package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;

import javax.validation.constraints.NotNull;

/**
 * Created by xjn on 17/3/10.
 * 编辑请求读service
 */
public interface DoctorEventModifyRequestReadService {
    /**
     * 根据id获取事件编辑请求
     * @param requestId id
     * @return 事件编辑请求
     */
    Response<DoctorEventModifyRequest> findById(@NotNull(message = "requestId.not.null") Long requestId);
}
