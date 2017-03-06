package io.terminus.doctor.user.manager;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dao.DoctorServiceStatusDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorStaffWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.doctor.user.service.SubRoleWriteService;
import io.terminus.parana.user.address.service.AddressReadService;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final DoctorStaffWriteService doctorStaffWriteService;
    private final DoctorServiceReviewDao doctorServiceReviewDao;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    private final DoctorServiceStatusDao doctorServiceStatusDao;
    private final AddressReadService addressReadService;
    private final UserDao userDao;
    private final SubRoleWriteService subRoleWriteService;

    @Autowired
    public DoctorServiceReviewManager(DoctorOrgDao doctorOrgDao,
                                      DoctorServiceReviewDao doctorServiceReviewDao,
                                      DoctorFarmDao doctorFarmDao, DoctorServiceStatusDao doctorServiceStatusDao,
                                      DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                                      AddressReadService addressReadService,
                                      DoctorStaffWriteService doctorStaffWriteService,
                                      DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService,
                                      UserDao userDao, SubRoleWriteService subRoleWriteService) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorServiceStatusDao = doctorServiceStatusDao;
        this.addressReadService = addressReadService;
        this.doctorStaffWriteService = doctorStaffWriteService;
        this.doctorUserDataPermissionWriteService = doctorUserDataPermissionWriteService;
        this.userDao = userDao;
        this.subRoleWriteService = subRoleWriteService;
    }

    @Transactional
    public void applyOpenService(BaseUser user, DoctorOrg org, DoctorServiceReview.Type type, DoctorServiceReview  review, String realName){
        //校验入参
        Preconditions.checkArgument(org != null, "required.org.info.missing");

        DoctorOrg exist = doctorOrgDao.findByName(org.getName());
        if (exist == null) {
            doctorOrgDao.create(org);
        } else {
            org.setId(exist.getId());
            doctorOrgDao.update(org);
        }

        //添加状态变更记录
        this.createServiceReviewTrack(user.getId(), DoctorServiceReview.Status.REVIEW.getValue(), type, null);

        //更新状态为已提交,待审核
        review.setStatus(DoctorServiceReview.Status.REVIEW.getValue());
        review.setRealName(realName);
        doctorServiceReviewDao.update(review);
    }
    private void createDoctorStaff(Long userId, Long farmId) {
        DoctorStaff staff = new DoctorStaff();
        staff.setFarmId(farmId);
        staff.setUserId(userId);
        staff.setStatus(DoctorStaff.Status.PRESENT.value());
        doctorStaffWriteService.createDoctorStaff(staff);
    }
    private void createServiceReviewTrack(Long userId, Integer newStatus,
                                          DoctorServiceReview.Type type, String reason){
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
    public List<DoctorFarm> openDoctorService(BaseUser user, Long userId, String loginName, DoctorOrg org, List<DoctorFarm> farms){
        if (org == null || org.getId() == null) {
            throw new ServiceException("orgId.not.null");
        }

        User exist = userDao.findByName(loginName);
        if(exist != null && !exist.getId().equals(userId)){
            throw new ServiceException("duplicated.name"); // 用户名已存在
        }
        User primaryUser = userDao.findById(userId); // 被审核的主账号
        primaryUser.setName(loginName);
        userDao.update(primaryUser);

        List<DoctorFarm> newFarms = Lists.newArrayList(); //将被保存下来的猪场
        //保存猪场信息
        if(farms != null){
            farms.forEach(farm -> {
                if (farm.getName() == null || farm.getName().trim().isEmpty()) {
                    throw new ServiceException("farm.name.not.null");
                }
                farm.setOrgName(org.getName());
                farm.setOrgId(org.getId());
                if (farm.getProvinceId() != null) {
                    farm.setProvinceName(RespHelper.orServEx(addressReadService.findById(farm.getProvinceId())).getName());
                }
                if (farm.getCityId() != null) {
                    farm.setCityName(RespHelper.orServEx(addressReadService.findById(farm.getCityId())).getName());
                }
                if (farm.getDistrictId() != null) {
                    farm.setDistrictName(RespHelper.orServEx(addressReadService.findById(farm.getDistrictId())).getName());
                }
                if (farm.getId() != null) {
                    doctorFarmDao.update(farm);
                } else {
                    doctorFarmDao.create(farm);
                }
                newFarms.add(farm);
            });
        }
        List<Long> newFarmIds = newFarms.stream().map(DoctorFarm::getId).collect(Collectors.toList());
        String newFarmIdStr = Joiner.on(",").join(newFarmIds);
        //查询并保存permission
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
        if(permission != null){
            permission.getFarmIdsList().forEach(oldFarmId -> {
                if (!newFarmIds.contains(oldFarmId)) {
                    doctorFarmDao.delete(oldFarmId);
                }
            });
            permission.setFarmIds(newFarmIdStr);
            permission.setUpdatorId(user.getId());
            permission.setUpdatorName(user.getName());
            permission.setOrgIdsList(Lists.newArrayList(org.getId()));
            RespHelper.orServEx(doctorUserDataPermissionWriteService.updateDataPermission(permission));
        }else{
            permission = new DoctorUserDataPermission();
            permission.setUserId(userId);
            permission.setFarmIds(newFarmIdStr);
            permission.setCreatorName(user.getName());
            permission.setCreatorId(user.getId());
            permission.setUpdatorId(user.getId());
            permission.setUpdatorName(user.getName());
            permission.setOrgIdsList(Lists.newArrayList(org.getId()));
            doctorUserDataPermissionWriteService.createDataPermission(permission);
        }

        //更新审批状态, 记录track, 更新服务状态
        this.updateServiceReviewStatus(user, userId, DoctorServiceReview.Type.PIG_DOCTOR,
                DoctorServiceReview.Status.OK, null);

        // 初始化内置子账号角色权限
        newFarmIds.forEach(farmId -> {
            RespHelper.orServEx(subRoleWriteService.initDefaultRoles("MOBILE", userId, farmId));
        });
        return newFarms;
    }

    @Transactional
    public void updateServiceReviewStatus(BaseUser user, Long userId, DoctorServiceReview.Type type,
                                          DoctorServiceReview.Status newStatus, String reason){
        if (Objects.equals(newStatus.getValue(), DoctorServiceReview.Status.REVIEW.getValue())) {
            doctorServiceReviewDao.updateStatus(userId, type, newStatus);
            this.createServiceReviewTrack(userId, newStatus.getValue(), type, reason);
        } else {
            doctorServiceReviewDao.updateStatus(userId, user.getId(), type, newStatus);
            this.createServiceReviewTrack(userId, newStatus.getValue(), type, reason);
        }
    }

}
