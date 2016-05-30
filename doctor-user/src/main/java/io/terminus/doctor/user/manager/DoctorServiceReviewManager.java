package io.terminus.doctor.user.manager;

import com.google.common.base.Preconditions;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorStaff;
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


    @Autowired
    public DoctorServiceReviewManager(DoctorOrgDao doctorOrgDao, DoctorStaffDao doctorStaffDao,
                                      DoctorServiceReviewDao doctorServiceReviewDao) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorStaffDao = doctorStaffDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
    }

    @Transactional
    public void applyOpenService(BaseUser user, DoctorOrg org, DoctorServiceReview.Type type){
        if (Objects.equals(DoctorServiceReview.Type.PIG_DOCTOR.getValue(), type.getValue())) {
            //校验入参
            Preconditions.checkArgument(org != null, "required.org.info.missing");
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
        staff.setStatus(1);
        staff.setCreatorId(user.getId());
        staff.setCreatorName(user.getName());
        staff.setUpdatorId(user.getId());
        staff.setUpdatorName(user.getName());
        doctorStaffDao.create(staff);
    }
}
