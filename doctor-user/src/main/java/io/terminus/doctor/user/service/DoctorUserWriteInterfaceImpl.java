package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.model.RespDto;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.doctor.user.interfaces.service.DoctorUserWriteInterface;
import io.terminus.doctor.user.manager.UserInterfaceManager;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RpcProvider
public class DoctorUserWriteInterfaceImpl implements DoctorUserWriteInterface {

    private final UserDaoExt userDaoExt;
    private final UserInterfaceManager userInterfaceManager;

    @Autowired
    public DoctorUserWriteInterfaceImpl(UserDaoExt userDaoExt,
                                        UserInterfaceManager userInterfaceManager){
        this.userDaoExt = userDaoExt;
        this.userInterfaceManager = userInterfaceManager;
    }

    @Override
    public RespDto<Boolean> update(UserDto user) {
        RespDto<Boolean> response = new RespDto<>();
        try {
            if(user.getName() != null){
                User exist = userDaoExt.findByName(user.getName());
                if(exist != null && !exist.getId().equals(user.getId())){
                    return RespDto.fail("duplicated.name");
                }
            }
            if(user.getMobile() != null){
                User exist = userDaoExt.findByMobile(user.getMobile());
                if(exist != null && !exist.getId().equals(user.getId())){
                    return RespDto.fail("duplicated.mobile");
                }
            }
            if(user.getEmail() != null){
                User exist = userDaoExt.findByEmail(user.getEmail());
                if(exist != null && !exist.getId().equals(user.getId())){
                    return RespDto.fail("duplicated.email");
                }
            }

            userInterfaceManager.update(user);
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
            response.setResult(userInterfaceManager.create(user));
        } catch (Exception e) {
            log.error("create user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("create.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Boolean> delete(Long id) {
        RespDto<Boolean> response = new RespDto<>();
        try {
            userInterfaceManager.deletes(Lists.newArrayList(id));
            response.setResult(true);
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
            userInterfaceManager.deletes(ids);
            response.setResult(ids.size());
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
            List<Long> ids = Arrays.asList(idn);
            ids.add(id0);
            ids.add(id1);
            userInterfaceManager.deletes(ids);
            response.setResult(ids.size());
        } catch (Exception e) {
            log.error("update user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("delete.user.failed");
        }
        return response;
    }

}
