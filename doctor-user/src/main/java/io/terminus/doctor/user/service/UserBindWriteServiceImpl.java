package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.UserBindDao;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 陈增辉
 */
@Slf4j
@Service
@RpcProvider
public class UserBindWriteServiceImpl implements UserBindWriteService{

    private final UserBindDao userBindDao;

    @Autowired
    public UserBindWriteServiceImpl(UserBindDao userBindDao){
        this.userBindDao = userBindDao;
    }

    @Override
    public Response<Boolean> createUserBind(UserBind userBind) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(userBindDao.create(userBind));
        } catch (Exception e) {
            log.error("createUserBind failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("create.user.bind.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteUserBindById(Long id) {
        Response<Boolean> response = new Response<>();
        try {
            if (!userBindDao.delete(id)) {
                log.warn("no UserBind deleted, id = {}", id);
            }
            response.setResult(true);
        } catch (Exception e) {
            log.error("deleteUserBind failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.bind.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteUserBindByUserIdAndTargetSystem(Long userId, TargetSystem targetSystem) {
        Response<Boolean> response = new Response<>();
        try {
            UserBind userBind = userBindDao.findByUserIdAndTargetSystem(userId, targetSystem);
            if (userBind != null) {
                response.setResult(userBindDao.delete(userBind.getId()));
            } else {
                return Response.fail("no.user.bind.found");
            }
        } catch (Exception e) {
            log.error("deleteUserBind failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.bind.failed");
        }
        return response;
    }

}
