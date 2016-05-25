package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.user.interfaces.service.DoctorUserWriteInterface;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.model.Response;
import io.terminus.doctor.user.interfaces.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DoctorUserWriteInterfaceImpl implements DoctorUserWriteInterface {
    private final UserDaoExt userDaoExt;


    @Autowired
    public DoctorUserWriteInterfaceImpl(UserDaoExt userDaoExt){
        this.userDaoExt = userDaoExt;
    }

    @Override
    public Response<Integer> updateStatus(Long userId, Integer status) {
        Response<Integer> response = new Response<>();
        try {
            response.setResult(userDaoExt.updateStatus(userId, status));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public Response<Integer> batchUpdateStatus(List<Long> userIds, Integer status) {
        Response<Integer> response = new Response<>();
        try {
            response.setResult(userDaoExt.batchUpdateStatus(userIds, status));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public Response<Integer> updateType(Long userId, int type) {
        Response<Integer> response = new Response<>();
        try {
            userDaoExt.updateRoles(userId, this.getRolesFromType(type));
            response.setResult(1);
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public Response<Integer> batchUpdateType(List<Long> userIds, int type) {
        Response<Integer> response = new Response<>();
        try {
            userDaoExt.updateRoles(userIds, this.getRolesFromType(type));
            response.setResult(userIds.size());
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> update(User user) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(userDaoExt.update(BeanMapper.map(user, io.terminus.parana.user.model.User.class)));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> createUser(User user) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(userDaoExt.create(BeanMapper.map(user, io.terminus.parana.user.model.User.class)));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("create.user.failed");
        }
        return response;
    }

    @Override
    public Response<Integer> createUsers(List<User> users) {
        Response<Integer> response = new Response<>();
        try {
            response.setResult(userDaoExt.creates(BeanMapper.mapList(users, io.terminus.parana.user.model.User.class)));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("create.user.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(userDaoExt.delete(id));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.failed");
        }
        return response;
    }

    @Override
    public Response<Integer> deletes(List<Long> ids) {
        Response<Integer> response = new Response<>();
        try {
            response.setResult(userDaoExt.deletes(ids));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.failed");
        }
        return response;
    }

    @Override
    public Response<Integer> deletes(Long id0, Long id1, Long... idn) {
        Response<Integer> response = new Response<>();
        try {
            response.setResult(userDaoExt.deletes(id0, id1, idn));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.failed");
        }
        return response;
    }

    private List<String> getRolesFromType(int type){
        return null; //TODO
    }
}
