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
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final ServiceReviewTrackDao serviceReviewTrackDao;

    @Autowired
    public DoctorServiceReviewManager(DoctorOrgDao doctorOrgDao, DoctorStaffDao doctorStaffDao,
                                      DoctorServiceReviewDao doctorServiceReviewDao,
                                      DoctorFarmDao doctorFarmDao,
                                      DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                                      ServiceReviewTrackDao serviceReviewTrackDao) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorStaffDao = doctorStaffDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.serviceReviewTrackDao = serviceReviewTrackDao;
    }

    @Transactional
    public void applyOpenService(BaseUser user, DoctorOrg org, DoctorServiceReview.Type type, DoctorServiceReview  review){
        if (Objects.equals(DoctorServiceReview.Type.PIG_DOCTOR.getValue(), type.getValue())) {
            //校验入参
            Preconditions.checkArgument(org != null, "required.org.info.missing");
            //保存org
            DoctorStaff staff = doctorStaffDao.findByUserId(user.getId());
            if (staff == null || staff.getOrgId() == null) {
                doctorOrgDao.create(org);
                //插入staff
                this.createDoctorStaff(user, org.getId(), org.getName());
            } else {
                org.setId(staff.getOrgId());
                doctorOrgDao.update(org);

                staff.setUpdatorName(user.getName());
                staff.setUpdatorId(user.getId());
                staff.setOrgName(org.getName());
                doctorStaffDao.update(staff);
            }
        } else if (Objects.equals(DoctorServiceReview.Type.PIGMALL.getValue(), type.getValue())
                || Objects.equals(DoctorServiceReview.Type.NEVEREST.getValue(), type.getValue())) {
            //TODO extra things. 陈增辉: 目前没什么额外的数据需要处理,以后如有需要可在此添加
        } else {
            throw new ServiceException("doctor.service.review.type.error");
        }
        //更新状态为已提交,待审核
        doctorServiceReviewDao.updateStatus(user.getId(), type, DoctorServiceReview.Status.REVIEW);
        //添加状态变更记录
        this.createServiceReviewTrack(null, user.getId(), review.getStatus(), DoctorServiceReview.Status.REVIEW.getValue(), type, "用户申请开通服务");

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
    private void createServiceReviewTrack(BaseUser user, Long userId, Integer oldStatus, Integer newStatus,
                                          DoctorServiceReview.Type type, String reason){
        ServiceReviewTrack track = new ServiceReviewTrack();
        track.setUserId(userId);
        track.setType(type.getValue());
        track.setOldStatus(oldStatus);
        track.setNewStatus(newStatus);
        if(user != null){
            track.setReviewerId(user.getId());
            track.setReviewerName(user.getName());
        }
        track.setReason(reason);
        serviceReviewTrackDao.create(track);
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
                farmIds = Joiner.on(",").join(list.stream().map(DoctorFarm::getId).toArray());
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

        this.updateServiceStatus(user, userId, DoctorServiceReview.Type.PIG_DOCTOR, DoctorServiceReview.Status.REVIEW,
                DoctorServiceReview.Status.OK, "审核通过");
    }

    @Transactional
    public void updateServiceStatus(BaseUser user, Long userId, DoctorServiceReview.Type type, DoctorServiceReview.Status oldStatus,
                                    DoctorServiceReview.Status newStatus, String reason){
        if (Objects.equals(newStatus.getValue(), DoctorServiceReview.Status.REVIEW.getValue())) {
            doctorServiceReviewDao.updateStatus(userId, type, newStatus);
            this.createServiceReviewTrack(null, userId, oldStatus.getValue(), newStatus.getValue(), type, reason);
        } else {
            doctorServiceReviewDao.updateStatus(userId, user.getId(), type, newStatus);
            this.createServiceReviewTrack(user, userId, oldStatus.getValue(), newStatus.getValue(), type, reason);
        }
    }
}
