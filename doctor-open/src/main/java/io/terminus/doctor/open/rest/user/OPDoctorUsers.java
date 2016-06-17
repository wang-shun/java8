package io.terminus.doctor.open.rest.user;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.open.dto.DoctorServiceReviewDto;
import io.terminus.doctor.open.dto.ServiceReviewOpenDto;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.dto.DoctorMenuDto;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.service.*;
import io.terminus.doctor.user.service.business.DoctorServiceReviewService;
import io.terminus.doctor.web.core.dto.ServiceBetaStatusToken;
import io.terminus.doctor.web.core.service.ServiceBetaStatusHandler;
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
    private final DoctorServiceStatusReadService doctorServiceStatusReadService;
    private final PrimaryUserReadService primaryUserReadService;
    private final ServiceBetaStatusHandler serviceBetaStatusHandler;

    @Autowired
    public OPDoctorUsers(DoctorServiceReviewReadService doctorServiceReviewReadService,
                         DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                         DoctorUserReadService doctorUserReadService,
                         DoctorServiceReviewService doctorServiceReviewService,
                         DoctorOrgReadService doctorOrgReadService,
                         DoctorServiceStatusReadService doctorServiceStatusReadService,
                         DoctorMobileMenuReadService doctorMobileMenuReadService,
                         AclLoader aclLoader,
                         PermissionHelper permissionHelper,
                         PrimaryUserReadService primaryUserReadService,
                         ServiceBetaStatusHandler serviceBetaStatusHandler) {
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceReviewService = doctorServiceReviewService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorServiceStatusReadService = doctorServiceStatusReadService;
        this.doctorMobileMenuReadService = doctorMobileMenuReadService;
        this.aclLoader = aclLoader;
        this.permissionHelper = permissionHelper;
        this.primaryUserReadService = primaryUserReadService;
        this.serviceBetaStatusHandler = serviceBetaStatusHandler;
    }

    /**
     * 根据用户id查询 用户服务开通情况
     * @return 服务开通情况
     */
    @OpenMethod(key = "get.user.service.status")
    public DoctorServiceReviewDto getUserServiceStatus() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        Long primaryUserId; //主账号id

        if(Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), baseUser.getType())){
            //当前用户是主账号,则直接查询
            primaryUserId = baseUser.getId();
        }else if(Objects.equals(UserType.FARM_SUB.value(), baseUser.getType())){
            //当前用户是子账号, 找他的主账号
            primaryUserId = OPRespHelper.orOPEx(primaryUserReadService.findSubByUserId(baseUser.getId())).getParentUserId();
        }else{
            throw new JsonResponseException("authorize.fail");
        }
        DoctorServiceReviewDto dto = new DoctorServiceReviewDto();
        dto.setUserId(primaryUserId);
        DoctorServiceStatus serviceStatus = OPRespHelper.orOPEx(doctorServiceStatusReadService.findByUserId(primaryUserId));
        OPRespHelper.orOPEx(doctorServiceReviewReadService.findServiceReviewsByUserId(primaryUserId)).forEach(review -> {
            ServiceReviewOpenDto innerDto = BeanMapper.map(review, ServiceReviewOpenDto.class);
            switch (DoctorServiceReview.Type.from(review.getType())) {
                case PIG_DOCTOR:
                    innerDto.setServiceStatus(serviceStatus.getPigdoctorStatus());
                    innerDto.setReason(serviceStatus.getPigdoctorReason());
                    dto.setPigDoctor(innerDto);
                    break;
                case PIGMALL:
                    innerDto.setServiceStatus(serviceStatus.getPigmallStatus());
                    innerDto.setReason(serviceStatus.getPigmallReason());
                    dto.setPigmall(innerDto);
                    break;
                case NEVEREST:
                    innerDto.setServiceStatus(serviceStatus.getNeverestStatus());
                    innerDto.setReason(serviceStatus.getNeverestReason());
                    dto.setNeverest(innerDto);
                    break;
                case PIG_TRADE:
                    innerDto.setServiceStatus(serviceStatus.getPigtradeStatus());
                    innerDto.setReason(serviceStatus.getPigtradeReason());
                    dto.setPigTrade(innerDto);
                    break;
            }
        });
        return dto;
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
        ServiceBetaStatusToken token = serviceBetaStatusHandler.getServiceBetaStatusToken();
        if(token.inBeta(DoctorServiceReview.Type.from(serviceApplyDto.getType()))){
            throw new OPClientException("service.in.beta");
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
