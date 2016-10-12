package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dao.DoctorHistoryMessageDao;
import io.terminus.doctor.msg.model.DoctorHistoryMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiao on 16/10/11.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorHistoryMessageWriteServiceImpl implements DoctorHistoryMessageWriteService {
    @Autowired
    private DoctorHistoryMessageDao doctorHistoryMessageDao;
    @Override
    public Response<Long> createHistoryMessage(DoctorHistoryMessage doctorHistoryMessage) {
        try {
            doctorHistoryMessageDao.create(doctorHistoryMessage);
            return Response.ok(doctorHistoryMessage.getId());
        } catch (Exception e) {
            log.error("create.history.message.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.history.message.failed");
        }
    }
}
