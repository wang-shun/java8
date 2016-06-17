package io.terminus.doctor.user.manager;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dao.*;
import io.terminus.doctor.user.model.*;
import io.terminus.doctor.user.service.DoctorStaffWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.parana.user.address.service.AddressReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final DoctorStaffWriteService doctorStaffWriteService;
    private final DoctorServiceReviewDao doctorServiceReviewDao;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    private final ServiceReviewTrackDao serviceReviewTrackDao;
    private final DoctorServiceStatusDao doctorServiceStatusDao;
    private final AddressReadService addressReadService;

    @Autowired
    public DoctorServiceReviewManager(DoctorOrgDao doctorOrgDao, DoctorStaffDao doctorStaffDao,
                                      DoctorServiceReviewDao doctorServiceReviewDao,
                                      DoctorFarmDao doctorFarmDao, DoctorServiceStatusDao doctorServiceStatusDao,
                                      DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                                      ServiceReviewTrackDao serviceReviewTrackDao,
                                      AddressReadService addressReadService,
                                      DoctorStaffWriteService doctorStaffWriteService,
                                      DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorStaffDao = doctorStaffDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.serviceReviewTrackDao = serviceReviewTrackDao;
        this.doctorServiceStatusDao = doctorServiceStatusDao;
        this.addressReadService = addressReadService;
        this.doctorStaffWriteService = doctorStaffWriteService;
        this.doctorUserDataPermissionWriteService = doctorUserDataPermissionWriteService;
    }

    @Transactional
    public void applyOpenService(BaseUser user, DoctorOrg org, DoctorServiceReview.Type type, DoctorServiceReview  review, String realName){
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
                RespHelper.orServEx(doctorStaffWriteService.updateDoctorStaff(staff));
            }
        } else if (Objects.equals(DoctorServiceReview.Type.PIGMALL.getValue(), type.getValue())
                || Objects.equals(DoctorServiceReview.Type.NEVEREST.getValue(), type.getValue())) {
            //TODO extra things. 目前没什么额外的数据需要处理,以后如有需要可在此添加
        } else if (Objects.equals(DoctorServiceReview.Type.PIG_TRADE.getValue(), type.getValue())) {
            //TODO extra things. 目前没什么额外的数据需要处理,以后如有需要可在此添加
        } else {
            throw new ServiceException("doctor.service.review.type.error");
        }

        //添加状态变更记录
        this.createServiceReviewTrack(null, user.getId(), review.getStatus(), DoctorServiceReview.Status.REVIEW.getValue(), type, null);

        //更新状态为已提交,待审核
        review.setStatus(DoctorServiceReview.Status.REVIEW.getValue());
        review.setRealName(realName);
        doctorServiceReviewDao.update(review);
    }
    private void createDoctorStaff(BaseUser user, Long orgId, String orgName){
        DoctorStaff staff = new DoctorStaff();
        staff.setOrgId(orgId);
        staff.setOrgName(orgName);
        staff.setUserId(user.getId());
        staff.setStatus(DoctorStaff.Status.PRESENT.value());
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

        DoctorServiceStatus status = doctorServiceStatusDao.findByUserId(userId);
        switch (type) {
            case PIG_DOCTOR:
                this.setPigDoctorField(status, newStatus, reason);
                break;
            case PIGMALL:
                this.setPigmallField(status, newStatus, reason);
                break;
            case NEVEREST:
                this.setNeverestField(status, newStatus, reason);
                break;
            case PIG_TRADE:
                this.setPigTradeField(status, newStatus, reason);
                break;
        }
        doctorServiceStatusDao.updateWithNull(status);
    }
    private void setPigDoctorField(DoctorServiceStatus status, Integer newStatus, String reason){
        status.setPigdoctorReviewStatus(newStatus);
        status.setPigdoctorReason(reason);
        if(Objects.equals(newStatus, DoctorServiceReview.Status.OK.getValue())){
            status.setPigdoctorStatus(DoctorServiceStatus.Status.OPENED.value());
        }
    }
    private void setPigmallField(DoctorServiceStatus status, Integer newStatus, String reason){
        status.setPigmallReviewStatus(newStatus);
        status.setPigmallReason(reason);
        if(Objects.equals(newStatus, DoctorServiceReview.Status.OK.getValue())){
            status.setPigmallStatus(DoctorServiceStatus.Status.OPENED.value());
        }
    }
    private void setNeverestField(DoctorServiceStatus status, Integer newStatus, String reason){
        status.setNeverestReviewStatus(newStatus);
        status.setNeverestReason(reason);
        if(Objects.equals(newStatus, DoctorServiceReview.Status.OK.getValue())){
            status.setNeverestStatus(DoctorServiceStatus.Status.OPENED.value());
        }
    }
    private void setPigTradeField(DoctorServiceStatus status, Integer newStatus, String reason){
        status.setPigtradeReviewStatus(newStatus);
        status.setPigtradeReason(reason);
        if(Objects.equals(newStatus, DoctorServiceReview.Status.OK.getValue())){
            status.setPigtradeStatus(DoctorServiceStatus.Status.OPENED.value());
        }
    }

    @Transactional
    public void openDoctorService(BaseUser user, Long userId, List<DoctorFarm> farms, DoctorOrg org){
        //更新org, 无论是否有修改,都可以update一下
        doctorOrgDao.update(org);

        //更新staff里的冗余字段orgName
        DoctorStaff staff = doctorStaffDao.findByUserId(userId);
        staff.setOrgName(org.getName());
        RespHelper.orServEx(doctorStaffWriteService.updateDoctorStaff(staff));

        List<Long> newFarmIds = Lists.newArrayList(); //将被保存下来的猪场
        //保存猪场信息
        if(farms != null){
            farms.stream().forEach(farm -> {
                farm.setOrgName(org.getName());
                farm.setOrgId(org.getId());
                farm.setProvinceName(RespHelper.orServEx(addressReadService.findById(farm.getProvinceId())).getName());
                farm.setCityName(RespHelper.orServEx(addressReadService.findById(farm.getCityId())).getName());
                farm.setDistrictName(RespHelper.orServEx(addressReadService.findById(farm.getDistrictId())).getName());
                if (farm.getId() != null) {
                    doctorFarmDao.update(farm);
                } else {
                    doctorFarmDao.create(farm);
                }
                newFarmIds.add(farm.getId());
            });
        }

        String newFarmIdStr = Joiner.on(",").join(newFarmIds);
        //查询并保存permission
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
        if(permission != null){
            permission.getFarmIdsList().stream().forEach(oldFarmId -> {
                if (!newFarmIds.contains(oldFarmId)) {
                    doctorFarmDao.delete(oldFarmId);
                }
            });
            permission.setFarmIds(newFarmIdStr);
            permission.setUpdatorId(user.getId());
            permission.setUpdatorName(user.getName());
            RespHelper.orServEx(doctorUserDataPermissionWriteService.updateDataPermission(permission));
        }else{
            permission = new DoctorUserDataPermission();
            permission.setUserId(userId);
            permission.setFarmIds(newFarmIdStr);
            permission.setCreatorName(user.getName());
            permission.setCreatorId(user.getId());
            permission.setUpdatorId(user.getId());
            permission.setUpdatorName(user.getName());
            doctorUserDataPermissionDao.create(permission);
        }

        //更新审批状态, 记录track, 更新服务状态
        this.updateServiceReviewStatus(user, userId, DoctorServiceReview.Type.PIG_DOCTOR, DoctorServiceReview.Status.REVIEW,
                DoctorServiceReview.Status.OK, null);
    }

    @Transactional
    public void updateServiceReviewStatus(BaseUser user, Long userId, DoctorServiceReview.Type type, DoctorServiceReview.Status oldStatus,
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
