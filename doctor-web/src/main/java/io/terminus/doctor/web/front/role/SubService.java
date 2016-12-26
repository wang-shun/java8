package io.terminus.doctor.web.front.role;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorStaffWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.doctor.user.service.PrimaryUserWriteService;
import io.terminus.pampas.client.Export;
import io.terminus.parana.common.utils.EncryptUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 子账号相关服务
 * Mail: houly@terminus.io
 * Data: 下午5:25 16/5/25
 * Author: houly
 */
@Slf4j
@Component
public class SubService {
    public static final Joiner AT = Joiner.on("@").skipNulls();

    private final DoctorUserReadService doctorUserReadService;

    private final UserWriteService<User> userWriteService;

    private final DoctorUserProfileReadService doctorUserProfileReadService;

    private final PrimaryUserReadService primaryUserReadService;
    private final PrimaryUserWriteService primaryUserWriteService;

    private final DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;

    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    private final DoctorStaffWriteService doctorStaffWriteService;
    private final DoctorStaffReadService doctorStaffReadService;

    @Autowired
    public SubService(DoctorUserReadService doctorUserReadService, UserWriteService<User> userWriteService,
                      DoctorUserProfileReadService doctorUserProfileReadService, PrimaryUserReadService primaryUserReadService,
                      DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService,
                      DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                      DoctorStaffWriteService doctorStaffWriteService,
                      DoctorStaffReadService doctorStaffReadService,
                      PrimaryUserWriteService primaryUserWriteService) {
        this.doctorUserReadService = doctorUserReadService;
        this.userWriteService = userWriteService;
        this.doctorUserProfileReadService = doctorUserProfileReadService;
        this.primaryUserReadService = primaryUserReadService;
        this.doctorUserDataPermissionWriteService = doctorUserDataPermissionWriteService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorStaffWriteService = doctorStaffWriteService;
        this.doctorStaffReadService = doctorStaffReadService;
        this.primaryUserWriteService = primaryUserWriteService;
    }

    public Response<Sub> findSubByUserId(BaseUser user, Long userId) {
        try {
            Long parentUserId = this.getPrimaryUserId(user);

            io.terminus.doctor.user.model.Sub sub = checkUserAndSubUser(parentUserId, userId);

            User u = RespHelper.orServEx(doctorUserReadService.findById(userId));
            UserProfile userProfile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(userId));
            DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));

            return Response.ok(makeSub(sub, u, userProfile, permission));
        } catch (ServiceException e) {
            log.warn("find sub failed, user={}, userId={}, error={}",
                    user, userId, e.getMessage());
            return Response.fail("sub.find.fail");
        } catch (Exception e) {
            log.error("find sub failed, user={}, userId={}, cause:{}",
                    user, userId, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.find.fail");
        }
    }

    private Sub makeSub(io.terminus.doctor.user.model.Sub sub, User u, UserProfile userProfile, DoctorUserDataPermission permission){
        Sub op = new Sub();
        if (u != null && userProfile != null) {
            op.setId(u.getId());
            op.setUsername(u.getName());
            op.setCreatedAt(u.getCreatedAt());
            op.setRoleId(sub.getRoleId());
            op.setRoleName(sub.getRoleName());
            op.setContact(sub.getContact());
            op.setRealName(userProfile.getRealName());
            op.setStatus(sub.getStatus());
            op.setBarnIds(permission.getBarnIdsList());
            op.setFarmIds(permission.getFarmIdsList());
        }
        return op;
    }

    /**
     * 更新子账号
     * @param user
     * @param sub
     * @return
     */
    public Response<Boolean> updateSub(BaseUser user, Sub sub){
        try {
            if(sub.getId() == null){
                Response.fail("sub.id.miss");
            }

            Long primaryId = this.getPrimaryUserId(user);

            User subUser = RespHelper.orServEx(doctorUserReadService.findById(sub.getId()));

            //子账号@主账号
            String userName = subAccount(sub, user);
            if(!Objects.equals(subUser.getName(), userName)){
                checkSubUserAccount(userName);
            }
            subUser.setName(userName);

            if(StringUtils.isNotBlank(sub.getContact())){
                Response<User> mobileRes = doctorUserReadService.findBy(sub.getContact(), LoginType.MOBILE);
                if(mobileRes.isSuccess() && mobileRes.getResult() != null && !Objects.equals(mobileRes.getResult().getId(), sub.getId())){
                    throw new JsonResponseException("user.register.mobile.has.been.used");
                }
                subUser.setMobile(sub.getContact());
            }

            this.updateSubStaffStatus(subUser, io.terminus.doctor.user.model.Sub.Status.from(sub.getStatus()));
            // TODO: 自定义角色冗余进 user 表
            List<String> roles = Lists.newArrayList("SUB");
            if (sub.getRoleId() != null) {
                roles.add("SUB(SUB(" + sub.getRoleId() + "))");
            }
            subUser.setRoles(roles);
            subUser.setExtra(MapBuilder.<String, String>of()
                    .put("pid", primaryId.toString())
                    .put("contact", sub.getContact())
                    .put("realName", sub.getRealName())
                    .map());
            RespHelper.orServEx(userWriteService.update(subUser));
            this.updateSubPermission(user, subUser.getId(), sub.getFarmIds(), sub.getBarnIds());
            return Response.ok(true);
        } catch (ServiceException | JsonResponseException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("update sub failed, user={}, sub={}, cause:{}",
                    user, sub, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.update.fail");
        }
    }

    private void updateSubStaffStatus(User subUser, io.terminus.doctor.user.model.Sub.Status status){
        DoctorStaff staff = RespHelper.orServEx(doctorStaffReadService.findStaffByUserId(subUser.getId()));
        io.terminus.doctor.user.model.Sub sub = RespHelper.orServEx(primaryUserReadService.findSubByUserId(subUser.getId()));
        sub.setStatus(status.value());

        if(Objects.equals(status.value(), io.terminus.doctor.user.model.Sub.Status.ACTIVE.value())){
            subUser.setStatus(UserStatus.NORMAL.value());
            staff.setStatus(DoctorStaff.Status.PRESENT.value());
        }else if(Objects.equals(status.value(), io.terminus.doctor.user.model.Sub.Status.ABSENT.value())){
            subUser.setStatus(UserStatus.LOCKED.value());
            staff.setStatus(DoctorStaff.Status.ABSENT.value());
        }else{
            throw new ServiceException("sub.user.status.error");
        }
        RespHelper.orServEx(primaryUserWriteService.updateSub(sub));
        RespHelper.orServEx(doctorStaffWriteService.updateDoctorStaff(staff));
    }

    private void updateSubPermission(BaseUser currentUser, Long userId, List<Long> farmIds, List<Long> barnIds){
        //先查下主账号的猪场, 以避免子账号的猪场不属于主账号
        List<Long> primaryFarms = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(currentUser.getId())).getFarmIdsList();
        for(Long farmId : farmIds){
            if(!primaryFarms.contains(farmId)){
                throw new ServiceException("authorize.fail");
            }
        }
        //先查再改
        DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
        permission.setFarmIds(Joiner.on(",").join(farmIds));
        if(barnIds != null && !barnIds.isEmpty()){
            permission.setBarnIds(Joiner.on(",").join(barnIds));
        }
        permission.setUpdatorId(currentUser.getId());
        permission.setUpdatorName(currentUser.getName());
        RespHelper.orServEx(doctorUserDataPermissionWriteService.updateDataPermission(permission));
    }

    /**
     * 创建子账号
     * @param user
     * @param sub
     * @return
     */
    public Response<Long> createSub(BaseUser user, Sub sub){
        try {
            Long primaryId = this.getPrimaryUserId(user);
            //先查下主账号的猪场, 以避免子账号的猪场不属于主账号
            DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(primaryId));
            List<Long> primaryFarms = permission.getFarmIdsList();
            for(Long farmId : sub.getFarmIds()){
                if(!primaryFarms.contains(farmId)){
                    throw new ServiceException("authorize.fail");
                }
            }

            User subUser = new User();

            //子账号@主账号
            String userName = subAccount(sub, user);
            checkSubUserAccount(userName);
            subUser.setName(userName);

            if(StringUtils.isNotBlank(sub.getContact())){
                Response<User> mobileRes = doctorUserReadService.findBy(sub.getContact(), LoginType.MOBILE);
                if(mobileRes.isSuccess() && mobileRes.getResult() != null){
                    throw new JsonResponseException("user.register.mobile.has.been.used");
                }
                subUser.setMobile(sub.getContact());
            }
            subUser.setPassword(sub.getPassword());
            subUser.setType(UserType.FARM_SUB.value());
            subUser.setStatus(UserStatus.NORMAL.value());
            // TODO: 自定义角色冗余进 user 表
            List<String> roles = Lists.newArrayList("SUB");
            if (sub.getRoleId() != null) {
                roles.add("SUB(SUB(" + sub.getRoleId() + "))");
            }
            subUser.setRoles(roles);
            subUser.setExtra(MapBuilder.<String, String>of()
                    .put("pid", primaryId.toString())
                    .put("contact", sub.getContact())
                    .put("realName", sub.getRealName())
                    .map());
            Long subUserId = RespHelper.orServEx(userWriteService.create(subUser));
            this.createPermission(user, subUserId, sub.getFarmIds(), sub.getBarnIds(), permission.getOrgIdsList());
            return Response.ok(subUserId);
        } catch (ServiceException | JsonResponseException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("creat sub failed, user={}, sub={}, cause:{}", user, sub, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.create.fail");
        }
    }

    private void createPermission(BaseUser currentUser, Long userId, List<Long> farmIds, List<Long> barnIds, List<Long> orgIds){
        //创建 数据权限
        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setUserId(userId);
        permission.setFarmIds(Joiner.on(",").join(farmIds));
        if(barnIds != null && !barnIds.isEmpty()){
            permission.setBarnIds(Joiner.on(",").join(barnIds));
        }
        permission.setCreatorId(currentUser.getId());
        permission.setCreatorName(currentUser.getName());
        permission.setUpdatorId(currentUser.getId());
        permission.setUpdatorName(currentUser.getName());
        permission.setOrgIdsList(orgIds);
        RespHelper.orServEx(doctorUserDataPermissionWriteService.createDataPermission(permission));
    }

    public Response<List<Sub>> findByConditions(BaseUser user, Long roleId, String roleName, String userName,
                                                String realName, Integer status, Integer limit){
        try{
            Long parentUserId = this.getPrimaryUserId(user);
            List<io.terminus.doctor.user.model.Sub> subList = RespHelper.orServEx(
                    primaryUserReadService.findByConditions(parentUserId, roleId, roleName, userName, realName, status, limit)
            );
            return Response.ok(this.setSubInfo(subList));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("find sub failed, user={}, cause:{}", user, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.find.fail");
        }
    }

    @Export(paramNames = {"user", "roleId", "pageNo", "pageSize"})
    public Response<Paging<Sub>> pagingSubs(BaseUser user, Long roleId,String roleName, String userName,
                                            String realName, Integer status, Integer pageNo, Integer pageSize) {
        try {
            Long parentUserId = this.getPrimaryUserId(user);

            Paging<io.terminus.doctor.user.model.Sub> paging = RespHelper.orServEx(
                    primaryUserReadService.subPagination(parentUserId, roleId, roleName, userName, realName,
                            status, pageNo, pageSize)
            );

            return Response.ok(new Paging<>(paging.getTotal(), this.setSubInfo(paging.getData())));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("paging sub failed, user={}, pageNo={}, pageSize={}, cause:{}", user, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.paging.fail");
        }
    }

    private List<Sub> setSubInfo(List<io.terminus.doctor.user.model.Sub> subList){
        List<Long> userIds = subList.stream().map(io.terminus.doctor.user.model.Sub::getUserId).collect(Collectors.toList());
        List<User> users = RespHelper.orServEx(doctorUserReadService.findByIds(userIds));

        List<UserProfile> profiles = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserIds(userIds));

        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, UserProfile> profileMap = profiles.stream().collect(Collectors.toMap(UserProfile::getUserId, u -> u));

        return subList.stream()
                .map(s -> {
                    User u = userMap.get(s.getUserId());
                    UserProfile up = profileMap.get(s.getUserId());
                    DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(s.getUserId()));
                    return makeSub(s, u, up, permission);
                })
                .collect(Collectors.toList());
    }

    /**
     * 重置员工密码
     * @param user
     * @param userId
     * @param resetPassword
     * @return
     */
    public Response<Boolean> resetPassword(BaseUser user, Long userId, String resetPassword){

        try {
            checkPasswordFormat(resetPassword);

            User subUser = RespHelper.orServEx(doctorUserReadService.findById(userId));

            subUser.setPassword(EncryptUtil.encrypt(resetPassword));

            return Response.ok(RespHelper.orServEx(userWriteService.update(subUser)));

        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("reset sub failed, user={}, userId={}, cause:{}",
                    user, userId, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.reset.fail");
        }
    }

    private io.terminus.doctor.user.model.Sub checkUserAndSubUser(Long parentUserId, Long subUserId){
        Optional<io.terminus.doctor.user.model.Sub> optional = RespHelper.orServEx(primaryUserReadService.findSubSellerByParentUserIdAndUserId(parentUserId, subUserId));
        if(!optional.isPresent()){
            throw new ServiceException("sub.not.belong");
        }
        return optional.get();
    }

    private void checkPasswordFormat(String password){
        if (!password.matches("[\\s\\S]{6,25}")){
            throw new ServiceException("user.password.6to25");
        }
    }

    /**
     * 获得子账号  子账号@主账号
     * @param sub
     * @param user
     * @return
     */
    private String subAccount(Sub sub, BaseUser user){
        User primaryUser;
        // 子账号特别处理
        if((Objects.equals(user.getType(), UserType.FARM_SUB.value()))){
            Long primaryUserId = this.getPrimaryUserId(user);
            primaryUser = RespHelper.orServEx(doctorUserReadService.findById(primaryUserId));
        }else{
            primaryUser = RespHelper.orServEx(doctorUserReadService.findById(user.getId()));
        }
        return AT.join(sub.getUsername(), primaryUser.getName() == null ? primaryUser.getMobile() : primaryUser.getName());
    }

    /**
     * 检查子账号是否存在, 如果存在则抛出异常
     * @param userName
     */
    private void checkSubUserAccount(String userName){
        User subUser = RespHelper.orServEx(doctorUserReadService.subAccountCheck(userName));
        if(subUser != null) {
            throw new ServiceException("sub.account.exist");
        }
    }

    public Long getPrimaryUserId(BaseUser user){
        Long parentUserId;
        if(Objects.equals(user.getType(), UserType.FARM_ADMIN_PRIMARY.value())){
            parentUserId = user.getId();
        }else if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
            parentUserId = RespHelper.orServEx(primaryUserReadService.findSubByUserId(user.getId())).getParentUserId();
        }else{
            throw new ServiceException("authorize.fail");
        }
        return parentUserId;
    }
}
