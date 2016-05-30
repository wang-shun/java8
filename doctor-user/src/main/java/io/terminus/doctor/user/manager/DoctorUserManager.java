package io.terminus.doctor.user.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.util.UserRoleUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.user.dao.*;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.*;
import io.terminus.parana.common.utils.Iters;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.model.User;
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

    private final OperatorDao operatorDao;

    private final PrimaryUserDao primaryUserDao;

    private final SubDao subDao;
    private final DoctorServiceReviewDao doctorServiceReviewDao;
    private final DoctorOrgDao doctorOrgDao;
    private final DoctorStaffDao doctorStaffDao;

    @Autowired
    public DoctorUserManager(UserDao userDao, OperatorDao operatorDao, PrimaryUserDao primaryUserDao, SubDao subDao,
                             DoctorServiceReviewDao doctorServiceReviewDao, DoctorOrgDao doctorOrgDao,
                             DoctorStaffDao doctorStaffDao) {
        this.userDao = userDao;
        this.operatorDao = operatorDao;
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.doctorOrgDao = doctorOrgDao;
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
            primaryUserDao.create(primaryUser);

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

            Sub sub = new Sub();
            sub.setUserId(userId);
            sub.setRoleId(roleId);
            sub.setParentUserId(Params.get(user.getExtra(), "pid"));
            subDao.create(sub);

        }
        return userId;
    }

    @Transactional
    public Boolean update(User user) {
        return userDao.update(user);
    }

    @Transactional
    public void applyOpenService(BaseUser user, DoctorServiceApplyDto serviceApplyDto){
        DoctorServiceReview.Type type = DoctorServiceReview.Type.from(serviceApplyDto.getType());
        if (type == null) {
            log.error("doctor service review type error, type = {}", serviceApplyDto.getType());
            throw new ServiceException("doctor.service.review.type.error");
        }
        if (Objects.equals(DoctorServiceReview.Type.PIG_DOCTOR.getValue(), type.getValue())) {
            if (serviceApplyDto.getOrg() == null) {
                throw new ServiceException("required.org.info.missing");
            }
            DoctorOrg org = serviceApplyDto.getOrg();
            doctorOrgDao.create(org);
            this.createDoctorStaff(user, org.getId(), org.getName());
        } else if (Objects.equals(DoctorServiceReview.Type.PIGMALL.getValue(), type.getValue())
                || Objects.equals(DoctorServiceReview.Type.NEVEREST.getValue(), type.getValue())) {
            //no extra thing to do
        } else {
            throw new ServiceException("doctor.service.review.type.error");
        }
        doctorServiceReviewDao.updateStatus(user.getId(), null, type, DoctorServiceReview.Status.REVIEW);
    }
    private void createDoctorStaff(BaseUser user, Long orgId, String orgName){
        DoctorStaff staff = new DoctorStaff();
        staff.setOrgId(orgId);
        staff.setOrgName(orgName);
        staff.setUserId(user.getId());
        staff.setRoleId(null); //TODO
        staff.setRoleName(null); //TODO
        staff.setStatus(1);
        staff.setCreatorId(user.getId());
        staff.setCreatorName(user.getName());
        staff.setUpdatorId(user.getId());
        staff.setUpdatorName(user.getName());
        doctorStaffDao.create(staff);
    }

}
