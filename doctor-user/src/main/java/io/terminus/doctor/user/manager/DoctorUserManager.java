package io.terminus.doctor.user.manager;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.UserRoleUtil;
import io.terminus.doctor.user.dao.*;
import io.terminus.doctor.user.dto.IotUserDto;
import io.terminus.doctor.user.model.*;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.parana.common.utils.Iters;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * @author Effet
 */
@Slf4j
@Component
public class DoctorUserManager {
    private final UserDaoExt userDao;

    private final UserProfileDao userProfileDao;

    private final OperatorDao operatorDao;

    private final PrimaryUserDao primaryUserDao;

    private final SubDao subDao;

    private final SubRoleReadService subRoleReadService;

    private final IotUserDao iotUserDao;

    private final DoctorOrgDao orgDao;

    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Autowired
    public DoctorUserManager(UserDaoExt userDao,
                             UserProfileDao userProfileDao,
                             OperatorDao operatorDao,
                             PrimaryUserDao primaryUserDao,
                             SubDao subDao,
                             SubRoleReadService subRoleReadService, IotUserDao iotUserDao, DoctorOrgDao orgDao, DoctorUserDataPermissionDao doctorUserDataPermissionDao) {
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
        this.operatorDao = operatorDao;
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
        this.subRoleReadService = subRoleReadService;
        this.iotUserDao = iotUserDao;
        this.orgDao = orgDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
    }

    @Transactional
    public Long create(User user) {
        checkState(userDao.create(user), "create user failed, %", user);
        Long userId = user.getId();
        // TODO: update roles for different user type
        if (Objects.equals(user.getType(), UserType.OPERATOR.value())) {
            Long roleId = null;// TODO: read roleId from user.getRoles()
            for (String role : Iters.nullToEmpty(user.getRoles())) {
                List<String> richRole = UserRoleUtil.roleConsFrom(role);
                if (richRole.get(0).equalsIgnoreCase("ADMIN") && richRole.size() > 1) {
                    roleId = Long.parseLong(UserRoleUtil.roleConsFrom(richRole.get(1)).get(1));
                }
            }

            Operator operator = new Operator();
            operator.setUserId(userId);
            operator.setRoleId(roleId);
            operatorDao.create(operator);
        } else if (Objects.equals(user.getType(), UserType.NORMAL.value())) {
            if (user.getRoles().contains(UserRole.BUYER.name())) {
                // 买家
            }

            if (user.getRoles().contains(UserRole.SELLER.name())) {
                // 卖家
            }
        } else if (Objects.equals(user.getType(), UserType.FARM_ADMIN_PRIMARY.value())) {
            //猪场管理员
            PrimaryUser primaryUser = new PrimaryUser();
            primaryUser.setUserId(userId);
            //暂时暂定手机号
            primaryUser.setUserName(user.getMobile());
            String realName = user.getName();
            if (notNull(user.getExtra()) && user.getExtra().containsKey("realName")) {
                realName = Params.get(user.getExtra(), "realName");
            }
            primaryUser.setRealName(realName);
            primaryUser.setStatus(UserStatus.NORMAL.value());
            primaryUserDao.create(primaryUser);

            //用户个人信息
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfileDao.create(userProfile);
        } else if (Objects.equals(user.getType(), UserType.FARM_SUB.value())) {
            //猪场子账号
            Long roleId = null;// TODO: read roleId from user.getRoles()
            for (String role : Iters.nullToEmpty(user.getRoles())) {
                List<String> richRole = UserRoleUtil.roleConsFrom(role);
                if (richRole.get(0).equalsIgnoreCase("SUB") && richRole.size() > 1) {
                    roleId = Long.parseLong(UserRoleUtil.roleConsFrom(richRole.get(1)).get(1));
                }
            }
            SubRole subRole = RespHelper.orServEx(subRoleReadService.findById(roleId));

            Sub sub = new Sub();
            sub.setUserId(userId);
            sub.setUserName(user.getName());
            sub.setRealName(Params.get(user.getExtra(), "realName"));
            sub.setRoleId(roleId);
            sub.setRoleName(subRole.getName());
            sub.setParentUserId(Long.valueOf(Params.get(user.getExtra(), "pid")));
            sub.setContact(Params.get(user.getExtra(), "contact"));
            sub.setStatus(UserStatus.NORMAL.value());
            subDao.create(sub);

            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfile.setRealName(Params.get(user.getExtra(), "realName"));
            userProfileDao.create(userProfile);
        }
        return userId;
    }

    @Transactional
    public Boolean update(User user) {
        userDao.updateAll(user);

        if (Objects.equals(user.getType(), UserType.FARM_SUB.value())) {
            Sub sub = subDao.findIncludeFrozenByUserId(user.getId());
            if (isNull(sub)) {

                createSub(user);
            } else {
                //猪场子账号
                Long roleId = null;
                for (String role : Iters.nullToEmpty(user.getRoles())) {
                    List<String> richRole = UserRoleUtil.roleConsFrom(role);
                    if (richRole.get(0).equalsIgnoreCase("SUB") && richRole.size() > 1) {
                        roleId = Long.parseLong(UserRoleUtil.roleConsFrom(richRole.get(1)).get(1));
                    }
                }
                SubRole subRole = RespHelper.orServEx(subRoleReadService.findById(roleId));

                sub.setUserName(user.getName());
                sub.setRealName(Params.get(user.getExtra(), "realName"));
                sub.setRoleId(roleId);
                sub.setRoleName(subRole.getName());
                sub.setContact(Params.get(user.getExtra(), "contact"));
                sub.setFrozen(IsOrNot.NO.getKey());
                subDao.update(sub);
            }

            UserProfile userProfile = userProfileDao.findByUserId(user.getId());
            userProfile.setRealName(Params.get(user.getExtra(), "realName"));
            userProfileDao.update(userProfile);
        } else if (Objects.equals(user.getType(), UserType.FARM_ADMIN_PRIMARY.value())) {
            PrimaryUser primaryUser = primaryUserDao.findIncludeFrozenByUserId(user.getId());
            if (isNull(primaryUser)) {
                createPrimaryUser(user);
            } else {
                primaryUser.setFrozen(IsOrNot.NO.getKey());
                primaryUser.setRelFarmId(null);
                primaryUser.setUserName(user.getMobile());
                String realName = user.getName();
                if (notNull(user.getExtra()) && user.getExtra().containsKey("realName")) {
                    realName = Params.get(user.getExtra(), "realName");
                }
                primaryUser.setRealName(realName);
                primaryUser.setStatus(UserStatus.NORMAL.value());
                primaryUserDao.update(primaryUser);
            }


            //如果管理员账户用户更改了手机号，需要更新对应公司的手机号
            if (user.getExtra() != null && user.getExtra().containsKey("oldMobile")) {
                DoctorOrg org = orgDao.findByMobile(user.getExtra().get("oldMobile"));//修改之前的手机号
                if (null == org) {
                    log.warn("can not find org for update mobile to {}", user.getExtra().get("oldMobile"));
                } else {
                    org.setMobile(user.getMobile());
                    orgDao.update(org);
                }
            }
        }
        return true;
    }

    private void createSub(User user) {
        //猪场子账号
        Long roleId = null;// TODO: read roleId from user.getRoles()
        for (String role : Iters.nullToEmpty(user.getRoles())) {
            List<String> richRole = UserRoleUtil.roleConsFrom(role);
            if (richRole.get(0).equalsIgnoreCase("SUB") && richRole.size() > 1) {
                roleId = Long.parseLong(UserRoleUtil.roleConsFrom(richRole.get(1)).get(1));
            }
        }
        SubRole subRole = RespHelper.orServEx(subRoleReadService.findById(roleId));

        Sub sub = new Sub();
        sub.setUserId(user.getId());
        sub.setUserName(user.getName());
        sub.setRealName(Params.get(user.getExtra(), "realName"));
        sub.setRoleId(roleId);
        sub.setRoleName(subRole.getName());
        sub.setParentUserId(Long.valueOf(Params.get(user.getExtra(), "pid")));
        sub.setContact(Params.get(user.getExtra(), "contact"));
        sub.setStatus(UserStatus.NORMAL.value());
        subDao.create(sub);
    }

    private void createPrimaryUser(User user) {
        //猪场管理员
        PrimaryUser primaryUser = new PrimaryUser();
        primaryUser.setUserId(user.getId());
        //暂时暂定手机号
        primaryUser.setUserName(user.getMobile());
        String realName = user.getName();
        if (notNull(user.getExtra()) && user.getExtra().containsKey("realName")) {
            realName = Params.get(user.getExtra(), "realName");
        }
        primaryUser.setRealName(realName);
        primaryUser.setStatus(UserStatus.NORMAL.value());
        primaryUserDao.create(primaryUser);
    }

    @Transactional
    public User createIotUser(IotUserDto iotUserDto) {
        User user = new User();
        user.setName(iotUserDto.getUserName());
        user.setPassword(iotUserDto.getPassword());
        user.setType(UserType.IOT_OPERATOR.value());
        user.setStatus(UserStatus.NORMAL.value());
        user.setMobile(iotUserDto.getMobile());
        userDao.create(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getId());
        userProfile.setRealName(iotUserDto.getUserRealName());
        userProfileDao.create(userProfile);

        iotUserDto.setUserId(user.getId());
        iotUserDto.setType(IotUser.TYPE.IOT_OPERATOR.getValue());
        iotUserDto.setStatus(Sub.Status.ACTIVE.value());
        iotUserDao.create(iotUserDto);
        return user;
    }

    @Transactional
    public void updateIotUser(IotUserDto iotUserDto) {
        UserProfile userProfile = userProfileDao.findByUserId(iotUserDto.getUserId());
        UserProfile updateUser = new UserProfile();
        updateUser.setId(userProfile.getId());
        updateUser.setRealName(iotUserDto.getUserRealName());
        userProfileDao.update(updateUser);

        iotUserDao.update(iotUserDto);
    }

    public void checkExist(String mobile, String name) {

        User userByName = userDao.findByName(name);
        if (notNull(userByName) && !Objects.equals(userByName.getMobile(), mobile)) {
            throw new JsonResponseException("用户名已存在:" + name);
        }

        User userByMobile = userDao.findByMobile(mobile);
        if (isNull(userByMobile)) {
            return;
        }

        if (!(notNull(userByMobile.getExtra()) && userByMobile.getExtra().containsKey("frozen")
                && userByMobile.getExtra().get("frozen").equals(IsOrNot.YES.getKey().toString()))) {
            throw new JsonResponseException("手机号已存在:" + mobile);
        }
    }
}
