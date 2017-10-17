package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.user.dao.IotRoleDao;
import io.terminus.doctor.user.dao.IotUserDao;
import io.terminus.doctor.user.dto.IotUserDto;
import io.terminus.doctor.user.interfaces.event.DoctorSystemCode;
import io.terminus.doctor.user.interfaces.event.EventType;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.doctor.user.manager.DoctorUserManager;
import io.terminus.doctor.user.manager.UserInterfaceManager;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created by xjn on 17/10/11.
 */
@Slf4j
@Service
@RpcProvider
public class IotUserRoleWriteServiceImpl implements IotUserRoleWriteService {
    private final IotUserDao iotUserDao;
    private final IotRoleDao iotRoleDao;
    private final DoctorUserManager doctorUserManager;
    private final UserInterfaceManager userInterfaceManager;

    @Autowired
    public IotUserRoleWriteServiceImpl(IotUserDao iotUserDao, IotRoleDao iotRoleDao, DoctorUserManager doctorUserManager, UserInterfaceManager userInterfaceManager) {
        this.iotUserDao = iotUserDao;
        this.iotRoleDao = iotRoleDao;
        this.doctorUserManager = doctorUserManager;
        this.userInterfaceManager = userInterfaceManager;
    }

    @Override
    public Response<Boolean> createIotRole(IotRole iotRole) {
        try {
            return Response.ok(iotRoleDao.create(iotRole));
        } catch (Exception e) {
            log.error("create iot role failed, iotRole:{}, cause:{}",
                    iotRole, Throwables.getStackTraceAsString(e));
            return Response.fail("create.iot.role.failed");
        }
    }

    @Override
    public Response<Boolean> updateIotRole(IotRole iotRole) {
        try {
            iotRoleDao.update(iotRole);
            return Response.ok(iotUserDao.updateIotRoleName(iotRole.getId(), iotRole.getName()));
        } catch (Exception e) {
            log.error("update iot role failed, iotRole:{}, cause:{}",
                    iotRole, Throwables.getStackTraceAsString(e));
            return Response.fail("update.iot.role.failed");
        }
    }

    @Override
    public Response<Boolean> createIotUser(IotUserDto iotUserDto) {
        try {
            User user = doctorUserManager.createIotUser(iotUserDto);
            userInterfaceManager.pulishZkEvent(BeanMapper.map(user, UserDto.class), EventType.CREATE, DoctorSystemCode.PIG_DOCTOR);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create.iot.role.failed,iotUserDto:{}, cause:{}",
                    iotUserDto, Throwables.getStackTraceAsString(e));
            return Response.fail("create.iot.user.role.failed");
        }
    }

    @Override
    public Response<Boolean> updateIotUser(IotUserDto iotUserDto) {
        try {
            User user = doctorUserManager.updateIotUser(iotUserDto);
            userInterfaceManager.pulishZkEvent(BeanMapper.map(user, UserDto.class), EventType.CREATE, DoctorSystemCode.PIG_DOCTOR);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update iot user role failed, iotUser:{}, cause:{}",
                    iotUserDto, Throwables.getStackTraceAsString(e));
            return Response.fail("update.iot.user.role.failed");
        }
    }
}
