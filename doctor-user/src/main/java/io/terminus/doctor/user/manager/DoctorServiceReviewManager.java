package io.terminus.doctor.user.manager;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.user.dao.*;
import io.terminus.doctor.user.model.*;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 陈增辉 16/5/30.
 * 用户开通\关闭\冻结服务相关的事务类
 */
@Slf4j
@Component
public class DoctorServiceReviewManager {

    private final DoctorOrgDao doctorOrgDao;
    private final DoctorStaffDao doctorStaffDao;
    private final DoctorServiceReviewDao doctorServiceReviewDao;
    private final UserProfileDao userProfileDao;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Autowired
    public DoctorServiceReviewManager(DoctorOrgDao doctorOrgDao, DoctorStaffDao doctorStaffDao,
                                      DoctorServiceReviewDao doctorServiceReviewDao,
                                      UserProfileDao userProfileDao, DoctorFarmDao doctorFarmDao,
                                      DoctorUserDataPermissionDao doctorUserDataPermissionDao) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorStaffDao = doctorStaffDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.userProfileDao = userProfileDao;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
    }

    @Transactional
    public void applyOpenService(BaseUser user, DoctorOrg org, DoctorServiceReview.Type type, String realName){
        if (Objects.equals(DoctorServiceReview.Type.PIG_DOCTOR.getValue(), type.getValue())) {
            //校验入参
            Preconditions.checkArgument(org != null, "required.org.info.missing");
            //保存org
            doctorOrgDao.create(org);
            //保存staff
            this.createDoctorStaff(user, org.getId(), org.getName());
        } else if (Objects.equals(DoctorServiceReview.Type.PIGMALL.getValue(), type.getValue())
                || Objects.equals(DoctorServiceReview.Type.NEVEREST.getValue(), type.getValue())) {
            //no extra thing to do
        } else {
            throw new ServiceException("doctor.service.review.type.error");
        }
        //更新状态为已提交,待审核
        doctorServiceReviewDao.updateStatus(user.getId(), type, DoctorServiceReview.Status.REVIEW);

        //保存真实姓名
        if (Arguments.notEmpty(realName)) {
            UserProfile userProfile = userProfileDao.findByUserId(user.getId());
            if (userProfile != null) {
                userProfile.setRealName(realName);
                userProfileDao.update(userProfile);
            }
        }
    }
    private void createDoctorStaff(BaseUser user, Long orgId, String orgName){
        DoctorStaff staff = new DoctorStaff();
        staff.setOrgId(orgId);
        staff.setOrgName(orgName);
        staff.setUserId(user.getId());
        staff.setStatus(1);
        staff.setCreatorId(user.getId());
        staff.setCreatorName(user.getName());
        staff.setUpdatorId(user.getId());
        staff.setUpdatorName(user.getName());
        doctorStaffDao.create(staff);
    }

    @Transactional
    public void openDoctorService(BaseUser user, Long userId, List<String> farms, DoctorOrg org){
        doctorOrgDao.update(org);
        String farmIds = null;
        if (farms != null) {
            List<DoctorFarm> list = farms.stream().map(farmName -> {
                DoctorFarm farm = new DoctorFarm();
                farm.setOrgId(org.getId());
                farm.setOrgName(org.getName());
                farm.setName(farmName);
                return farm;
            }).collect(Collectors.toList());
            doctorFarmDao.creates(list);
            if (list.size() > 0) {
                farmIds = Joiner.on(",").join(Arrays.asList(list.stream().mapToLong(DoctorFarm::getId).toArray()));
            }
        }
        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setUserId(userId);
        permission.setFarmIds(farmIds);
        permission.setCreatorName(user.getName());
        permission.setCreatorId(user.getId());
        permission.setUpdatorId(user.getId());
        permission.setUpdatorName(user.getName());
        doctorUserDataPermissionDao.create(permission);
        doctorServiceReviewDao.updateStatus(userId, user.getId(), DoctorServiceReview.Type.PIG_DOCTOR, DoctorServiceReview.Status.OK);
    }
}
