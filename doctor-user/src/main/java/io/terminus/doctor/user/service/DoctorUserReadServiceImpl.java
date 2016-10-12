package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.enums.RoleType;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.impl.service.UserReadServiceImpl;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserProfileReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
@Primary
@RpcProvider
public class DoctorUserReadServiceImpl extends UserReadServiceImpl implements DoctorUserReadService{

    private final UserDao userDao;
    private final UserDaoExt userDaoExt;
    private final DoctorStaffReadService doctorStaffReadService;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    private final UserProfileReadService userProfileReadService;

    @Autowired
    public DoctorUserReadServiceImpl(UserDao userDao, DoctorStaffReadService doctorStaffReadService,
                                     DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                                     UserProfileReadService userProfileReadService,
                                     UserDaoExt userDaoExt) {
        super(userDao);
        this.userDao = userDao;
        this.doctorStaffReadService = doctorStaffReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.userProfileReadService = userProfileReadService;
        this.userDaoExt = userDaoExt;
    }

    /**
     * 根据用户标识查询客户
     *
     * @param loginId   用户标识
     * @param loginType 用户标志类型
     * @return 对应的用户
     */
    @Override
    public Response<User> findBy(String loginId, LoginType loginType) {
        try {
            User user;
            switch (loginType) {
                case NAME:
                    user = userDao.findByName(loginId);
                    break;
                case EMAIL:
                    user = userDao.findByEmail(loginId);
                    break;
                case MOBILE:
                    user = userDao.findByMobile(loginId);
                    break;
                default:
                    user = RespHelper.orServEx(subAccountCheck(loginId));
                    break;
            }
            if (user == null) {
                log.error("user(loginId={}, loginType={}) not found", loginId, loginType);
                return Response.fail("user.not.found");
            }
            return Response.ok(user);
        }catch(ServiceException e){
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to find user(loginId={}, loginType={}), cause:{}",
                    loginId, loginType, Throwables.getStackTraceAsString(e));
            return Response.fail("user.find.fail");
        }
    }

    @Override
    public Response<User> subAccountCheck(String loginId){
        try {
            List<String> strings = Splitters.AT.splitToList(loginId);
            if(strings.size() != 2){
                throw new ServiceException("sub.account.not.avalid");
            }
            //检查主账号是否存在
            User parentUser = userDao.findByName(strings.get(1));
            if (parentUser == null) {
                log.error("user(loginId={}, loginType=subaccount check puser) not found", loginId);
                throw new ServiceException("puser.not.found");
            }
            //检查子账号
            User user = userDao.findByName(loginId);

            return Response.ok(user);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to check subuser(loginId={}), cause:{}",
                    loginId, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.check.fail");
        }

    }

    @Override
    public Response<Integer> findUserRoleTypeByUserId(Long userId) {
        Response<Integer> response = new Response<>();
        try{
            User user = userDao.findById(userId);
            //管理员
            if(Objects.equals(UserType.ADMIN.value(), user.getType())){
                return Response.ok(RoleType.ADMIN.getValue());
            }

            //主账号
            if(Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), user.getType())){
                return Response.ok(RoleType.MAIN.getValue());
            }

            //子账号
            if(Objects.equals(UserType.FARM_SUB.value(), user.getType())){
                DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
                if(permission != null && !permission.getFarmIdsList().isEmpty()){
                    if(permission.getFarmIdsList().size() == 1){
                        return Response.ok(RoleType.SUB_SINGLE.getValue());
                    }
                    else{
                        return Response.ok(RoleType.SUB_MULTI.getValue());
                    }
                }else{
                    return Response.ok(RoleType.SUB_NONE.getValue());
                }
            }
            //其他
            return Response.fail("user.role.not.vaild");
        }catch(ServiceException e){
            response.setError(e.getMessage());
        }catch(Exception e){
            log.error("findUserRoleTypeByUserId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.role.type.by.user.id.failed");
        }
        return response;
    }

    @Override
    public Response<DoctorUserInfoDto> findUserInfoByUserId(Long userId) {
        Response<DoctorUserInfoDto> response = new Response<>();
        try {

            //用户信息
            User user = userDao.findById(userId);
            user.setPassword(null);

            //用户个人信息
            UserProfile userProfile = RespHelper.orServEx(userProfileReadService.findProfileByUserId(userId));

            //员工信息
            DoctorStaff doctorStaff = RespHelper.orServEx(this.findStaffByUserId(userId));

            //角色类型
            Integer roleType = RespHelper.orServEx(findUserRoleTypeByUserId(userId));

            Long farmId = null;
            if(Objects.equals(roleType, RoleType.SUB_SINGLE.getValue())){
                DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
                farmId = permission.getFarmIdsList().get(0);
            }

            return RespHelper.ok(
                    DoctorUserInfoDto.builder()
                            .user(user)
                            .userProfile(userProfile)
                            .staff(doctorStaff)
                            .frontRoleType(roleType)
                            .farmId(farmId)
                            .build()
            );
        }catch(ServiceException e){
            response.setError(e.getMessage());
        }catch(Exception e){
            log.error("findUserInfoByUserId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.info.by.user.id.failed");
        }
        return response;
    }

    @Override
    public Response<DoctorStaff> findStaffByUserId(@NotNull(message = "userId.not.null") Long userId) {
        return doctorStaffReadService.findStaffByUserId(userId);
    }

    @Override
    public Response<List<User>> listCreatedUserSince(Date since){
        try{
            return Response.ok(userDaoExt.listCreatedSince(since));
        }catch(Exception e){
            log.error("list created user since {} failed, cause : {}", since, Throwables.getStackTraceAsString(e));
            return Response.fail("list.created.user.failed");
        }
    }
}
