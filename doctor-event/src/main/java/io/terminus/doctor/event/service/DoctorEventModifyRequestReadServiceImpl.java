package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;

import javax.validation.constraints.NotNull;

/**
 * Created by xjn on 17/3/11.
 * 编辑事件请求读事件
 */
@RpcProvider
public class DoctorEventModifyRequestReadServiceImpl implements DoctorEventModifyRequestReadService {
    @Override
    public Response<DoctorEventModifyRequest> findById(@NotNull(message = "requestId.not.null") Long requestId) {
        return null;
    }
}
