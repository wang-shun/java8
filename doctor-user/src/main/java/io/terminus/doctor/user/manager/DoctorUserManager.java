package io.terminus.doctor.user.manager;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.UserRoleUtil;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.IotUserDao;
import io.terminus.doctor.user.dao.OperatorDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.dto.IotUserDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.IotUser;
import io.terminus.doctor.user.model.Operator;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.parana.common.utils.EncryptUtil;
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

    private final DoctorServiceReviewDao doctorServiceReviewDao;

    @Autowired
    public DoctorUserManager(UserDaoExt userDao,
                             UserProfileDao userProfileDao,
                             OperatorDao operatorDao,
                             PrimaryUserDao primaryUserDao,
                             SubDao subDao,
                             SubRoleReadService subRoleReadService, IotUserDao iotUserDao, DoctorOrgDao orgDao, DoctorUserDataPermissionDao doctorUserDataPermissionDao, DoctorServiceReviewDao doctorServiceReviewDao) {
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
        this.operatorDao = operatorDao;
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
        this.subRoleReadService = subRoleReadService;
        this.iotUserDao = iotUserDao;
        this.orgDao = orgDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
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
//            PrimaryUser primaryUser = new PrimaryUser();
//            primaryUser.setUserId(userId);
//            //暂时暂定手机号
//            primaryUser.setUserName(user.getMobile());
//            String realName = user.getName();
//            if (notNull(user.getExtra()) && user.getExtra().containsKey("realName")) {
//                realName = Params.get(user.getExtra(), "realName");
//            }
//            primaryUser.setRealName(realName);
//            primaryUser.setStatus(UserStatus.NORMAL.value());
//            primaryUserDao.create(primaryUser);

            Sub sub=new Sub();
            sub.setUserId(userId);
            sub.setUserName(user.getName());
            sub.setRealName(Params.get(user.getExtra(), "realName"));
            sub.setContact(Params.get(user.getExtra(), "contact"));
            sub.setUserType(2);
            sub.setStatus(UserStatus.NORMAL.value());
            subDao.create(sub);

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
            //sub.setParentUserId(Long.valueOf(Params.get(user.getExtra(), "pid")));
            sub.setParentUserId(91L);
            sub.setContact(Params.get(user.getExtra(), "contact"));
            sub.setStatus(UserStatus.NORMAL.value());
            sub.setUserType(3);
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
                if (notNull(user.getExtra()) && user.getExtra().containsKey("frozen")
                        && Objects.equals(user.getExtra().get("frozen"), IsOrNot.YES.getKey().toString())) {
                    sub.setFrozen(IsOrNot.YES.getKey());
                } else {
                    sub.setFrozen(IsOrNot.NO.getKey());
                }
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
                primaryUser.setRelFarmId(null);
                primaryUser.setUserName(user.getMobile());
                String realName = user.getName();
                if (notNull(user.getExtra()) && user.getExtra().containsKey("realName")) {
                    realName = Params.get(user.getExtra(), "realName");
                }
                primaryUser.setRealName(realName);
                primaryUser.setStatus(UserStatus.NORMAL.value());
                if (notNull(user.getExtra()) && user.getExtra().containsKey("frozen")
                        && Objects.equals(user.getExtra().get("frozen"), IsOrNot.YES.getKey().toString())) {
                    primaryUser.setFrozen(IsOrNot.YES.getKey());
                } else {
                    primaryUser.setFrozen(IsOrNot.NO.getKey());
                }
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

                //更新用户申请服务中绑定的手机号
                List<DoctorServiceReview> doctorServiceReviews = doctorServiceReviewDao.findByUserId(user.getId());
                if (!Arguments.isNullOrEmpty(doctorServiceReviews)) {
                    DoctorServiceReview updateReview = new DoctorServiceReview();
                    doctorServiceReviews.forEach(doctorServiceReview -> {
                        updateReview.setId(doctorServiceReview.getId());
                        updateReview.setUserMobile(user.getMobile());
                        doctorServiceReviewDao.update(updateReview);
                    });
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
        sub.setUserType(3);
        subDao.create(sub);
    }

    private void createPrimaryUser(User user) {
        //猪场管理员
//        PrimaryUser primaryUser = new PrimaryUser();
//        primaryUser.setUserId(user.getId());
//        //暂时暂定手机号
//        primaryUser.setUserName(user.getMobile());
//        String realName = user.getName();
//        if (notNull(user.getExtra()) && user.getExtra().containsKey("realName")) {
//            realName = Params.get(user.getExtra(), "realName");
//        }
//        primaryUser.setRealName(realName);
//        primaryUser.setStatus(UserStatus.NORMAL.value());
//        primaryUserDao.create(primaryUser);
        Sub sub=new Sub();
        sub.setUserId(user.getId());
        sub.setUserName(user.getName());
        sub.setRealName(Params.get(user.getExtra(), "realName"));
        sub.setContact(Params.get(user.getExtra(), "contact"));
        sub.setUserType(2);
        sub.setStatus(UserStatus.NORMAL.value());
        subDao.create(sub);
    }

    @Transactional
    public User createIotUser(IotUserDto iotUserDto) {
        User user = new User();
        user.setName(iotUserDto.getUserName());

        user.setPassword(EncryptUtil.encrypt(iotUserDto.getPassword()));
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

        User user = userDao.findById(iotUserDto.getUserId());
        user.setStatus(iotUserDto.getStatus());
        userDao.update(user);


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
