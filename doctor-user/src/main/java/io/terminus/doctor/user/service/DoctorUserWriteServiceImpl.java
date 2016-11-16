package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.user.interfaces.event.DoctorSystemCode;
import io.terminus.doctor.user.interfaces.event.EventType;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.doctor.user.manager.DoctorUserManager;
import io.terminus.doctor.user.manager.UserInterfaceManager;
import io.terminus.parana.common.utils.EncryptUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author Effet
 */
@Slf4j
@Service
@Primary
public class DoctorUserWriteServiceImpl implements UserWriteService<User> {

    private final DoctorUserManager doctorUserManager;
    private final UserInterfaceManager userInterfaceManager;

    @Autowired
    public DoctorUserWriteServiceImpl(DoctorUserManager doctorUserManager,
                                      UserInterfaceManager userInterfaceManager) {
        this.doctorUserManager = doctorUserManager;
        this.userInterfaceManager = userInterfaceManager;
    }

    @Override
    public Response<Long> create(User user) {
        try {
            if (StringUtils.hasText(user.getPassword())) {  //对密码加盐加密
                user.setPassword(EncryptUtil.encrypt(user.getPassword()));
            }
            Long userId = doctorUserManager.create(user);
            userInterfaceManager.pulishZkEvent(BeanMapper.map(user, UserDto.class), EventType.CREATE, DoctorSystemCode.PIG_DOCTOR);
            return Response.ok(userId);
        } catch (DuplicateKeyException e) {
            log.error("failed to create {}, cause:{}", user, Throwables.getStackTraceAsString(e));
            return Response.fail("user.loginId.duplicate");
        } catch (Exception e) {
            log.error("failed to create {}, cause:{}", user, Throwables.getStackTraceAsString(e));
            return Response.fail("user.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(User user) {
        try {
            doctorUserManager.update(user);
            userInterfaceManager.pulishZkEvent(BeanMapper.map(user, UserDto.class), EventType.UPDATE, DoctorSystemCode.PIG_DOCTOR);
            return Response.ok(true);
        } catch (Exception e) {
            log.error("failed to update {}, cause:{}", user, Throwables.getStackTraceAsString(e));
            return Response.fail("user.update.fail");
        }
    }
}
