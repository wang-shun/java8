package io.terminus.doctor.user.manager;

import com.google.common.base.Preconditions;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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

    @Autowired
    public DoctorServiceReviewManager(DoctorOrgDao doctorOrgDao, DoctorStaffDao doctorStaffDao,
                                      DoctorServiceReviewDao doctorServiceReviewDao,
                                      UserProfileDao userProfileDao) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorStaffDao = doctorStaffDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.userProfileDao = userProfileDao;
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
}
