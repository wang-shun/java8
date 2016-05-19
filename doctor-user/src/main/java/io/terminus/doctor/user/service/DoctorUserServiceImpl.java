package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.UserBindDao;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 陈增辉
 */
@Slf4j
@Service
public class DoctorUserServiceImpl implements  DoctorUserService{

    private final UserBindDao userBindDao;

    @Autowired
    public DoctorUserServiceImpl(UserBindDao userBindDao){
        this.userBindDao = userBindDao;
    }

    @Override
    public Response<Long> createUserBind(UserBind userBind) {
        Response<Long> response = new Response<>();
        try {
            userBindDao.create(userBind);
            response.setResult(userBind.getId());
        } catch (Exception e) {
            log.error("createUserBind failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("create.user.bind.failed");
        }
        return response;
    }

    @Override
    public Response<Long> deleteUserBindById(Long id) {
        Response<Long> response = new Response<>();
        try {
            userBindDao.delete(id);
            response.setResult(id);
        } catch (Exception e) {
            log.error("deleteUserBind failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.bind.failed");
        }
        return response;
    }

    @Override
    public Response<Long> deleteUserBindByUserIdAndTargetSystem(Long userId, TargetSystem targetSystem) {
        Response<Long> response = new Response<>();
        try {
            UserBind userBind = userBindDao.findByUserIdAndTargetSystem(userId, targetSystem);
            if (userBind != null) {
                userBindDao.delete(userBind.getId());
                response.setResult(userBind.getId());
            } else {
                response.setResult(0L);
            }
        } catch (Exception e) {
            log.error("deleteUserBind failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.bind.failed");
        }
        return response;
    }

    @Override
    public Response<UserBind> findUserBindById(Long id) {
        Response<UserBind> response = new Response<>();
        try {
            response.setResult(userBindDao.findById(id));
        } catch (Exception e) {
            log.error("findUserBindById failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.bind.failed");
        }
        return response;
    }

    @Override
    public Response<List<UserBind>> findUserBindByUserId(Long userId) {
        Response<List<UserBind>> response = new Response<>();
        try {
            response.setResult(userBindDao.findByUserId(userId));
        } catch (Exception e) {
            log.error("findUserBindById failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.bind.failed");
        }
        return response;
    }

    @Override
    public Response<UserBind> findUserBindByUserIdAndTargetSystem(Long userId, TargetSystem targetSystem) {
        Response<UserBind> response = new Response<>();
        try {
            response.setResult(userBindDao.findByUserIdAndTargetSystem(userId, targetSystem));
        } catch (Exception e) {
            log.error("findUserBindById failed, cause: {}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.bind.failed");
        }
        return response;
    }
}
