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

@Slf4j
@Service
public class UserBindReadServiceImpl implements UserBindReadService{
    private final UserBindDao userBindDao;

    @Autowired
    public UserBindReadServiceImpl(UserBindDao userBindDao){
        this.userBindDao = userBindDao;
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
