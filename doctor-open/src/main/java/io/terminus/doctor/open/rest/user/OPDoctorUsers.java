package io.terminus.doctor.open.rest.user;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.dto.DoctorMenuDto;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.dto.DoctorServiceReviewDto;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorMobileMenuReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.business.DoctorServiceReviewService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import io.terminus.parana.auth.core.AclLoader;
import io.terminus.parana.auth.core.PermissionHelper;
import io.terminus.parana.auth.model.Acl;
import io.terminus.parana.auth.model.ParanaThreadVars;
import io.terminus.parana.auth.model.PermissionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * Desc: 用户相关
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@OpenBean
@SuppressWarnings("unused")
@Slf4j
public class OPDoctorUsers {

    private final DoctorServiceReviewReadService doctorServiceReviewReadService;

    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    private final DoctorServiceReviewService doctorServiceReviewService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorMobileMenuReadService doctorMobileMenuReadService;
    private final AclLoader aclLoader;
    private final PermissionHelper permissionHelper;

    @Autowired
    public OPDoctorUsers(DoctorServiceReviewReadService doctorServiceReviewReadService,
                         DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                         DoctorUserReadService doctorUserReadService,
                         DoctorServiceReviewService doctorServiceReviewService,
                         DoctorOrgReadService doctorOrgReadService,
                         DoctorMobileMenuReadService doctorMobileMenuReadService,
                         AclLoader aclLoader,
                         PermissionHelper permissionHelper) {
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceReviewService = doctorServiceReviewService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorMobileMenuReadService = doctorMobileMenuReadService;
        this.aclLoader = aclLoader;
        this.permissionHelper = permissionHelper;
    }

    /**
     * 根据用户id查询 用户服务开通情况
     * @return 服务开通情况
     */
    @OpenMethod(key = "get.user.service.status")
    public DoctorServiceReviewDto getUserServiceStatus() {
        return OPRespHelper.orOPEx(doctorServiceReviewReadService.findServiceReviewDtoByUserId(UserUtil.getUserId()));
    }

    /**
     * 申请开通服务, 首次申请和驳回后再次申请都可以用这个
     * @param serviceApplyDto 申请信息
     * @return 申请是否成功
     */
    @OpenMethod(key = "apply.open.service", paramNames = "serviceApplyDto")
    public Boolean applyOpenService(@Valid DoctorServiceApplyDto serviceApplyDto) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(!Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), baseUser.getType())){
            //只有主账号(猪场管理员)才能申请开通服务
            throw new OPClientException("authorize.fail");
        }
        return OPRespHelper.orOPEx(doctorServiceReviewService.applyOpenService(baseUser, serviceApplyDto));
    }

    /**
     * 获取用户角色类型
     * @return 角色类型
     * @see io.terminus.doctor.user.enums.RoleType
     */
    @OpenMethod(key = "get.user.role.type")
    public Integer getUserRoleType() {
        return OPRespHelper.orOPEx(doctorUserReadService.findUserRoleTypeByUserId(UserUtil.getUserId()));
    }

    /**
     * 获取用户基本信息
     * @return 用户基本信息
     */
    @OpenMethod(key = "get.user.basic.info")
    public DoctorUserInfoDto getUserBasicInfo() {
        DoctorUserInfoDto doctorUserInfoDto = OPRespHelper.orOPEx(doctorUserReadService.findUserInfoByUserId(UserUtil.getUserId()));
        try {
            Acl acl = aclLoader.getAcl(ParanaThreadVars.getApp());
            BaseUser user = UserUtil.getCurrentUser();
            PermissionData perm = permissionHelper.getPermissions(acl, user, true);
            perm.setAllRequests(null); // empty it
            doctorUserInfoDto.setAuth(JsonMapper.nonEmptyMapper().toJson(perm));
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, JsonResponseException.class);
            log.error("get permissions of user failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new OPClientException("auth.permission.find.fail");
        }
        return doctorUserInfoDto;
    }

    /**
     * 查询用户所在的公司的信息
     * @return 公司id, 公司名称, 营业执照url, 公司手机号
     */
    @OpenMethod(key = "get.org.info")
    public DoctorOrg getOrgInfo() {
        return OPRespHelper.orOPEx(doctorOrgReadService.findOrgByUserId(UserUtil.getUserId()));
    }

    /**
     * 查询一级菜单
     * @return
     */
    @OpenMethod(key = "get.user.level.one.menu")
    public List<DoctorMenuDto> getUserLevelOneMenu() {
        return OPRespHelper.orOPEx(doctorMobileMenuReadService.findMenuByUserIdAndLevel(UserUtil.getUserId(), 1));
    }

}
