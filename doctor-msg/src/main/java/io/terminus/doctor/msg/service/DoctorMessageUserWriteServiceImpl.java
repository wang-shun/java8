package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dao.DoctorMessageUserDao;
import io.terminus.doctor.msg.model.DoctorMessageUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by xiao on 16/10/11.
 */
@Service
@Slf4j
@RpcProvider
public class DoctorMessageUserWriteServiceImpl implements DoctorMessageUserWriteService {
    @Autowired
    private DoctorMessageUserDao doctorMessageUserDao;
    @Override
    public Response<Long> createDoctorMessageUser(DoctorMessageUser doctorMessageUser) {
        try {
            doctorMessageUserDao.create(doctorMessageUser);
            return Response.ok(doctorMessageUser.getId());
        } catch (Exception e) {
            log.error("create doctor message user failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.doctor.message.user.failed");
        }
    }

    @Override
    public Response<Boolean> updateDoctorMessageUser(DoctorMessageUser doctorMessageUser) {
        try {
            return Response.ok(doctorMessageUserDao.update(doctorMessageUser));
        } catch (Exception e) {
            log.error("create doctor message user failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.doctor.message.user.failed");
        }
    }

    @Override
    public Response<Boolean> deleteByMessageId(@NotNull(message = "messageId is null") Long messageId) {
        try {
            doctorMessageUserDao.deleteByMessageId(messageId);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("delete by message id failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("delete.by.messageid.failed");
        }
    }

    @Override
    public Response<Boolean> deletesByMessageIds(List<Long> messageIds) {
        try {
            doctorMessageUserDao.deletesByMessageIds(messageIds);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("deletes by message ids failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("deletes.by.ids.failed");
        }
    }
}
