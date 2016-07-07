package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.model.RespDto;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.doctor.user.interfaces.service.DoctorUserWriteInterface;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RpcProvider
public class DoctorUserWriteInterfaceImpl implements DoctorUserWriteInterface {
    private final UserDaoExt userDaoExt;


    @Autowired
    public DoctorUserWriteInterfaceImpl(UserDaoExt userDaoExt){
        this.userDaoExt = userDaoExt;
    }

    @Override
    public RespDto<Integer> updateStatus(Long userId, Integer status) {
        RespDto<Integer> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.updateStatus(userId, status));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Integer> batchUpdateStatus(List<Long> userIds, Integer status) {
        RespDto<Integer> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.batchUpdateStatus(userIds, status));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Integer> updateType(Long userId, String typeName) {
        RespDto<Integer> response = new RespDto<>();
        try {
            User user = userDaoExt.findById(userId);
            if (user == null) {
                return RespDto.fail("user.not.found");
            }
            List<String> roles = user.getRoles();
            if (roles == null) {
                roles = new ArrayList<>();
            }else{
                roles = Lists.newArrayList(roles);
            }
            if(!roles.contains(typeName)){
                roles.add(0, typeName);
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
    public RespDto<Boolean> update(UserDto user) {
        RespDto<Boolean> response = new RespDto<>();
        try {
            User paranaUser = BeanMapper.map(user, User.class);
            userDaoExt.update(paranaUser);
            if(paranaUser.getRoles().size() > 0){
                String typeName = paranaUser.getRoles().get(0);
                this.updateType(paranaUser.getId(), typeName);
            }
            response.setResult(true);
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("update.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<UserDto> createUser(UserDto user) {
        RespDto<UserDto> response = new RespDto<>();
        try {
            if(userDaoExt.findByName(user.getName()) != null){
                return RespDto.fail("duplicated.name");
            }
            if(userDaoExt.findByMobile(user.getMobile()) != null){
                return RespDto.fail("duplicated.mobile");
            }
            if(userDaoExt.findByEmail(user.getEmail()) != null){
                return RespDto.fail("duplicated.email");
            }
            User paranaUser = this.makeParanaUserFromInterface(user);
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
    public RespDto<Boolean> delete(Long id) {
        RespDto<Boolean> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.delete(id));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Integer> deletes(List<Long> ids) {
        RespDto<Integer> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.deletes(ids));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Integer> deletes(Long id0, Long id1, Long... idn) {
        RespDto<Integer> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.deletes(id0, id1, idn));
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Boolean> removeRole(Long userId, String userTypeName){
        try{
            User user = userDaoExt.findById(userId);
            if (user == null) {
                return RespDto.fail("user.not.found");
            }
            List<String> roles = user.getRoles();
            if (roles == null) {
                return RespDto.ok(true);
            }else{
                roles = Lists.newArrayList(roles);
            }
            if(roles.contains(userTypeName)){
                roles.remove(userTypeName);
            }
            userDaoExt.updateRoles(userId, roles);
            return RespDto.ok(true);
        }catch(Exception e){
            log.error("remove user role failed, userId={}, role={}, cause:{}", userId, userTypeName, Throwables.getStackTraceAsString(e));
            return RespDto.fail("update.user.failed");
        }
    }

    private User makeParanaUserFromInterface(UserDto user){
        User paranaUser = BeanMapper.map(user, User.class);
        if(paranaUser.getType() == null){
            paranaUser.setType(UserType.NORMAL.value());
        }
        return paranaUser;
    }
    private List<User> makeParanaUserFromInterface(List<UserDto> users){
        List<User> list = users.stream().map(this::makeParanaUserFromInterface).collect(Collectors.toList());
        return list;
    }
}
