package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.enums.EventRequestStatus;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xjn on 17/3/10.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorEventModifyRequestWriteServiceImpl implements DoctorEventModifyRequestWriteService{
    @Autowired
    private DoctorEventModifyRequestDao eventModifyRequestDao;
    @Override
    public Response<Boolean> createRequest(DoctorEventModifyRequest modifyRequest) {
        try {
            modifyRequest.setStatus(EventRequestStatus.WAITING.getValue());
            return Response.ok(eventModifyRequestDao.create(modifyRequest));
        } catch (Exception e) {
            log.error("create request failed, modifyRequest:{}, cause:{}", modifyRequest, Throwables.getStackTraceAsString(e));
            return Response.fail("create.request.failed");
        }
    }
}
