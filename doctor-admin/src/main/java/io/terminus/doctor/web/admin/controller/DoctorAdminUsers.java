package io.terminus.doctor.web.admin.controller;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Joiners;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: admin分配权限的api
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/17
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/group-user")
public class DoctorAdminUsers {

    @RpcConsumer
    private DoctorUserReadService doctorUserReadService;
    @RpcConsumer
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    @RpcConsumer
    private DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private UserWriteService<User> userUserWriteService;
    @RpcConsumer
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    @RpcConsumer
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @RpcConsumer
    private DoctorServiceReviewReadService doctorServiceReviewReadService;

    /**
     * 新增集团用户
     * @param mobile    手机号
     * @param name      登录名
     * @param password  密码  默认手机号
     * @return 用户id
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Long addGroupUser(@RequestParam String mobile,
                             @RequestParam(required = false) String name,
                             @RequestParam(required = false) String password) {
        User user = checkGroupUser(mobile, name, password);
        Long userId = RespHelper.or500(userUserWriteService.create(user));
        initDefaultServiceStatus(userId, user.getMobile());
        return userId;
    }

    //初始化审核服务
    private void initDefaultServiceStatus(Long userId, String mobile) {
        DoctorServiceStatus status = new DoctorServiceStatus();
        status.setUserId(userId);
        //猪场软件初始状态
        status.setPigdoctorReviewStatus(DoctorServiceReview.Status.OK.getValue());
        status.setPigdoctorStatus(DoctorServiceStatus.Status.OPENED.value());
        //电商初始状态
        status.setPigmallStatus(DoctorServiceStatus.Status.BETA.value());
        status.setPigmallReason("敬请期待");
        status.setPigmallReviewStatus(DoctorServiceReview.Status.INIT.getValue());
        //大数据初始状态
        status.setNeverestStatus(DoctorServiceStatus.Status.BETA.value());
        status.setNeverestReason("敬请期待");
        status.setNeverestReviewStatus(DoctorServiceReview.Status.INIT.getValue());
        //生猪交易初始状态
        status.setPigtradeStatus(DoctorServiceStatus.Status.BETA.value());
        status.setPigtradeReason("敬请期待");
        status.setPigtradeReviewStatus(DoctorServiceReview.Status.INIT.getValue());
        RespHelper.or500(doctorServiceStatusWriteService.createServiceStatus(status));

        //初始化一些数据
        RespHelper.or500(doctorServiceReviewWriteService.initServiceReview(userId, mobile));
        DoctorServiceReview doctorReview = RespHelper.or500(doctorServiceReviewReadService.findServiceReviewByUserIdAndType(userId, DoctorServiceReview.Type.PIG_DOCTOR));
        if (doctorReview != null) {
            doctorReview.setStatus(DoctorServiceReview.Status.OK.getValue());
            RespHelper.or500(doctorServiceReviewWriteService.updateReview(doctorReview));
        }
    }

    //拼接集团用户数据
    private User checkGroupUser(String mobile, String name, String password) {
        Response<User> userResponse = doctorUserReadService.findBy(mobile, LoginType.MOBILE);
        if (userResponse.isSuccess()) {
            log.error("user existed, user:{}", userResponse.getResult());
            throw new JsonResponseException("user.is.existed");
        }
        User user = new User();
        user.setMobile(mobile);
        user.setName(name);

        //密码默认为手机号
        user.setPassword(StringUtils.hasText(password) ? password : mobile);
        user.setType(UserType.FARM_ADMIN_PRIMARY.value());
        user.setStatus(UserStatus.NORMAL.value());
        user.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));
        return user;
    }

    /**
     * 设置集团用户权限(新建与编辑)
     */
    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public Boolean groupUserAuth(@RequestParam Long userId,
                                 @RequestParam String farmIds) {
        User user = RespHelper.or500(doctorUserReadService.findById(userId));
        if (user == null) {
            log.error("admin add user auth, userId:({}) not found", userId);
            throw new JsonResponseException("user.not.found");
        }

        DoctorUserDataPermission permission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
        if (permission == null) {
            permission = getPermission(new DoctorUserDataPermission(), userId, farmIds);
            RespHelper.or500(doctorUserDataPermissionWriteService.createDataPermission(permission));
            return true;
        }
        return RespHelper.or500(doctorUserDataPermissionWriteService.updateDataPermission(getPermission(permission, userId, farmIds)));
    }

    private DoctorUserDataPermission getPermission(DoctorUserDataPermission permission, Long userId, String farmIds) {
        permission.setUserId(userId);
        permission.setFarmIds(MoreObjects.firstNonNull(farmIds, ""));
        permission.setOrgIds(getOrgIds(Splitters.splitToLong(farmIds, Splitters.COMMA)));
        return permission;
    }

    private String getOrgIds(List<Long> farmIds) {
        if (!notEmpty(farmIds)) {
            return "";
        }
        Set<Long> orgIds = farmIds.stream()
                .map(farmId -> {
                    DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
                    if (farm == null) {
                        log.error("get farm by id not found, farmId:{}", farmId);
                        throw new JsonResponseException("farm.not.found");
                    }
                    return farm.getOrgId();
                })
                .collect(Collectors.toSet());
        return Joiners.COMMA.join(orgIds);
    }
}
