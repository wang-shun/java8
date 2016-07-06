package io.terminus.doctor.web.front.role;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.pampas.client.Export;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.EncryptUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserProfileReadService;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    public SubService(DoctorUserReadService doctorUserReadService, UserWriteService<User> userWriteService,
                      DoctorUserProfileReadService doctorUserProfileReadService, PrimaryUserReadService primaryUserReadService) {
        this.doctorUserReadService = doctorUserReadService;
        this.userWriteService = userWriteService;
        this.doctorUserProfileReadService = doctorUserProfileReadService;
        this.primaryUserReadService = primaryUserReadService;
    }

    public Response<Sub> findSubByUserId(BaseUser user, Long userId) {
        try {
            Long parentUserId = user.getId();

            io.terminus.doctor.user.model.Sub sub = checkUserAndSubUser(parentUserId, userId);

            User u = RespHelper.orServEx(doctorUserReadService.findById(userId));

            UserProfile userProfile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(userId));


            return Response.ok(makeSub(sub, u, userProfile));
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

    private Sub makeSub(io.terminus.doctor.user.model.Sub sub, User u, UserProfile userProfile){
        Sub op = new Sub();
        if (u != null && userProfile != null) {
            op.setId(u.getId());
            op.setUsername(u.getName());
            op.setCreatedAt(u.getCreatedAt());
            op.setRoleId(sub.getRoleId());
            op.setRoleName(sub.getRoleName());
            op.setContact(sub.getContact());
            op.setRealName(userProfile.getRealName());
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

            Long primaryId = user.getId();

            User subUser = RespHelper.orServEx(doctorUserReadService.findById(sub.getId()));

            //子账号@主账号
            String userName = subAccount(sub, user);
            if(!Objects.equals(subUser.getName(), userName)){
                checkSubUserAccount(userName);
            }

            subUser.setName(userName);
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
            return userWriteService.update(subUser);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("update sub failed, user={}, sub={}, cause:{}",
                    user, sub, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.update.fail");
        }
    }

    /**
     * 创建子账号
     * @param user
     * @param sub
     * @return
     */
    public Response<Long> createSub(BaseUser user, Sub sub){
        try {
            Long primaryId = user.getId();

            User subUser = new User();

            //子账号@主账号
            String userName = subAccount(sub, user);

            checkSubUserAccount(userName);

            subUser.setName(userName);
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
            return userWriteService.create(subUser);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("creat sub failed, user={}, sub={}, cause:{}",
                    user, sub, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.create.fail");
        }
    }



    @Export(paramNames = {"user", "roleId", "pageNo", "pageSize"})
    public Response<Paging<Sub>> pagingSubs(BaseUser user, Long roleId,String roleName, String userName,
                                            String realName, Integer pageNo, Integer pageSize) {
        try {
            Long userId = user.getId();

            Paging<io.terminus.doctor.user.model.Sub> paging = RespHelper.orServEx(
                    primaryUserReadService.subPagination(userId, roleId, roleName, userName, realName, null, pageNo, pageSize)
            );

            List<Long> userIds = paging.getData().stream().map(s -> s.getUserId()).collect(Collectors.toList());
            List<User> users = RespHelper.orServEx(doctorUserReadService.findByIds(userIds));

            List<UserProfile> profiles = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserIds(userIds));

            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            Map<Long, UserProfile> profileMap = profiles.stream().collect(Collectors.toMap(UserProfile::getUserId, u -> u));
            List<Sub> result = paging.getData().stream().map(s -> {
                User u = userMap.get(s.getUserId());
                UserProfile up = profileMap.get(s.getUserId());
                return makeSub(s, u, up);
            }).collect(Collectors.toList());

            return Response.ok(new Paging<>(paging.getTotal(), result));
        } catch (ServiceException e) {
            log.error("paging sub failed, user={}, pageNo={}, pageSize={}, error={}", user, pageNo, pageSize, e.getMessage());
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("paging sub failed, user={}, pageNo={}, pageSize={}, cause:{}", user, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.paging.fail");
        }
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
            checkUserAndSubUser(user.getId(), userId);

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
        return AT.join(sub.getUsername(), ((DoctorUser)(user)).getMobile());
    }

    /**
     * 检查子账号是否存在
     * @param userName
     */
    private void checkSubUserAccount(String userName){
        User subUser = RespHelper.orServEx(doctorUserReadService.subAccountCheck(userName));
        if(subUser != null) {
            throw new ServiceException("sub.account.exist");
        }
    }
}
