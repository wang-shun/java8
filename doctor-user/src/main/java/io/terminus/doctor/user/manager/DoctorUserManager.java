package io.terminus.doctor.user.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.util.UserRoleUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.OperatorDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.SubRoleDao;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.Operator;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.parana.common.utils.Iters;
import io.terminus.parana.user.impl.dao.UserDao;
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

/**
 * @author Effet
 */
@Slf4j
@Component
public class DoctorUserManager {
    private final UserDao userDao;

    private final UserProfileDao userProfileDao;

    private final OperatorDao operatorDao;

    private final PrimaryUserDao primaryUserDao;

    private final SubDao subDao;

    private final SubRoleReadService subRoleReadService;

    private final DoctorServiceReviewDao doctorServiceReviewDao;

    @Autowired
    public DoctorUserManager(UserDao userDao, UserProfileDao userProfileDao, OperatorDao operatorDao, PrimaryUserDao primaryUserDao, SubDao subDao,
                             SubRoleReadService subRoleReadService, DoctorServiceReviewDao doctorServiceReviewDao, DoctorStaffDao doctorStaffDao) {
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
        this.operatorDao = operatorDao;
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
        this.subRoleReadService = subRoleReadService;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.doctorStaffDao = doctorStaffDao;
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
        } else if (Objects.equals(user.getType(), UserType.FARM_ADMIN_PRIMARY.value())){
            //猪场管理员
            PrimaryUser primaryUser = new PrimaryUser();
            primaryUser.setUserId(userId);
            //暂时暂定手机号
            primaryUser.setUserName(user.getMobile());
            primaryUser.setStatus(UserStatus.NORMAL.value());
            primaryUserDao.create(primaryUser);

            //用户个人信息
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfileDao.create(userProfile);

            //初始化4个服务, 均为未开通状态
            doctorServiceReviewDao.initData(userId);
        } else if (Objects.equals(user.getType(), UserType.FARM_SUB.value())){
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

            DoctorStaff pstaff = doctorStaffDao.findByUserId(Long.valueOf(Params.get(user.getExtra(), "pid")));

            DoctorStaff doctorStaff = new DoctorStaff();
            doctorStaff.setUserId(userId);
            doctorStaff.setOrgId(pstaff.getOrgId());
            doctorStaff.setOrgName(pstaff.getOrgName());
            doctorStaff.setStatus(pstaff.getStatus());
            doctorStaffDao.create(pstaff);

        }
        return userId;
    }

    @Transactional
    public Boolean update(User user) {
        userDao.update(user);

        if (Objects.equals(user.getType(), UserType.FARM_SUB.value())){
            //猪场子账号
            Long roleId = null;
            for (String role : Iters.nullToEmpty(user.getRoles())) {
                List<String> richRole = UserRoleUtil.roleConsFrom(role);
                if (richRole.get(0).equalsIgnoreCase("SUB") && richRole.size() > 1) {
                    roleId = Long.parseLong(UserRoleUtil.roleConsFrom(richRole.get(1)).get(1));
                }
            }
            SubRole subRole = RespHelper.orServEx(subRoleReadService.findById(roleId));

            Sub sub = subDao.findByUserId(user.getId());
            sub.setUserName(user.getName());
            sub.setRoleId(roleId);
            sub.setRoleName(subRole.getName());
            sub.setContact(Params.get(user.getExtra(), "contact"));
            subDao.update(sub);

            UserProfile userProfile = userProfileDao.findByUserId(user.getId());
            userProfile.setRealName(Params.get(user.getExtra(), "realName"));
            userProfileDao.update(userProfile);
        }
        return true;
    }

}
