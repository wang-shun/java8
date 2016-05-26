package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.model.Response;
import io.terminus.doctor.user.interfaces.model.User;
import io.terminus.doctor.user.interfaces.service.DoctorUserWriteInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public Response<Integer> updateType(Long userId, String typeName) {
        Response<Integer> response = new Response<>();
        try {
            io.terminus.parana.user.model.User user = userDaoExt.findById(userId);
            if (user == null) {
                return Response.fail("user.not.found");
            }
            List<String> roles = user.getRoles();
            if (roles == null) {
                roles = new ArrayList<>();
            }else{
                roles = Lists.newArrayList(roles);
            }
            if(!roles.contains(typeName)){
                roles.add(typeName);
                userDaoExt.updateRoles(userId, roles);
            }
            response.setResult(1);
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
            io.terminus.parana.user.model.User paranaUser = BeanMapper.map(user, io.terminus.parana.user.model.User.class);
            userDaoExt.update(paranaUser);
            if(paranaUser.getRolesJson() != null){
                userDaoExt.updateRoles(paranaUser.getId(), paranaUser.getRoles());
            }
            response.setResult(true);
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public Response<User> createUser(User user) {
        Response<User> response = new Response<>();
        try {
            if(userDaoExt.findByName(user.getName()) != null){
                return Response.fail("duplicated.name");
            }
            if(userDaoExt.findByMobile(user.getMobile()) != null){
                return Response.fail("duplicated.mobile");
            }
            if(userDaoExt.findByEmail(user.getEmail()) != null){
                return Response.fail("duplicated.email");
            }
            io.terminus.parana.user.model.User paranaUser = this.makeParanaUserFromInterface(user);
            userDaoExt.create(paranaUser);
            BeanMapper.copy(paranaUser, user);
            response.setResult(user);
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

    private io.terminus.parana.user.model.User makeParanaUserFromInterface(User user){
        io.terminus.parana.user.model.User paranaUser = BeanMapper.map(user, io.terminus.parana.user.model.User.class);
        if(paranaUser.getType() == null){
            paranaUser.setType(UserType.NORMAL.value());
        }
        return paranaUser;
    }
    private List<io.terminus.parana.user.model.User> makeParanaUserFromInterface(List<User> users){
        List<io.terminus.parana.user.model.User> list = users.stream().map(this::makeParanaUserFromInterface).collect(Collectors.toList());
        return list;
    }
}
