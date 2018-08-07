package io.terminus.doctor.open.rest.user;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.open.dto.DoctorServiceReviewDto;
import io.terminus.doctor.open.dto.ServiceReviewOpenDto;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.dto.DoctorMenuDto;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.IotUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorMobileMenuReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceStatusReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.IotUserRoleReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
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
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.*;

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
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    private final DoctorServiceReviewService doctorServiceReviewService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorMobileMenuReadService doctorMobileMenuReadService;
    private final AclLoader aclLoader;
    private final PermissionHelper permissionHelper;
    private final DoctorServiceStatusReadService doctorServiceStatusReadService;
    private final PrimaryUserReadService primaryUserReadService;
    private final ServiceBetaStatusHandler serviceBetaStatusHandler;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private IotUserRoleReadService iotUserRoleReadService;

    private final JedisTemplate jedisTemplate;

    @Value("${session.redis-prefix}")
    private String redisPrefix;

    @Value("${service-domain.pigiot:m.xrnm.com}")
    private String pigIotUrl;


    //猪场软件链接url
    private final String farmManageMultiple = "pigdoctor://company?homepage_type=1";
    ;

    @Autowired
    public OPDoctorUsers(DoctorServiceReviewReadService doctorServiceReviewReadService,
                         DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                         DoctorUserReadService doctorUserReadService,
                         DoctorServiceReviewService doctorServiceReviewService,
                         DoctorOrgReadService doctorOrgReadService,
                         DoctorServiceStatusReadService doctorServiceStatusReadService,
                         DoctorMobileMenuReadService doctorMobileMenuReadService,
                         AclLoader aclLoader,
                         PermissionHelper permissionHelper,
                         PrimaryUserReadService primaryUserReadService,
                         ServiceBetaStatusHandler serviceBetaStatusHandler, JedisTemplate jedisTemplate) {
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceReviewService = doctorServiceReviewService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorServiceStatusReadService = doctorServiceStatusReadService;
        this.doctorMobileMenuReadService = doctorMobileMenuReadService;
        this.aclLoader = aclLoader;
        this.permissionHelper = permissionHelper;
        this.primaryUserReadService = primaryUserReadService;
        this.serviceBetaStatusHandler = serviceBetaStatusHandler;
        this.jedisTemplate = jedisTemplate;
    }

    @Value("${service-domain.pigmall:m.xrnm.com}")
    private String pigmallURL;

    /**
     * 根据用户id查询 用户服务开通情况
     *
     * @return 服务开通情况
     */
    @OpenMethod(key = "get.user.service.status", paramNames = {"appKey"})
    public DoctorServiceReviewDto getUserServiceStatus(String appKey) {


        BaseUser baseUser = UserUtil.getCurrentUser();
        Long primaryUserId; //主账号id

        if (Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), baseUser.getType())) {
            //当前用户是主账号,则直接查询
            primaryUserId = baseUser.getId();
        } else if (Objects.equals(UserType.FARM_SUB.value(), baseUser.getType())) {
            //当前用户是子账号, 找他的主账号
            primaryUserId = OPRespHelper.orOPEx(primaryUserReadService.findSubByUserId(baseUser.getId())).getParentUserId();
        } else {
            IotUser iotUser = OPRespHelper.orOPEx(iotUserRoleReadService.findIotUserByUserId(baseUser.getId()));
            if (notNull(iotUser)) {
                DoctorServiceReviewDto dto = new DoctorServiceReviewDto();
                dto.setPigTrade(getPigIot(baseUser));
                return dto;
            }

            throw new OPClientException("authorize.fail");
        }
        DoctorServiceReviewDto dto = new DoctorServiceReviewDto();
        dto.setUserId(primaryUserId);
        DoctorServiceStatus serviceStatus = OPRespHelper.orOPEx(doctorServiceStatusReadService.findByUserId(primaryUserId));
        OPRespHelper.orOPEx(doctorServiceReviewReadService.findServiceReviewsByUserId(primaryUserId)).forEach(review -> {
            ServiceReviewOpenDto innerDto = BeanMapper.map(review, ServiceReviewOpenDto.class);
            switch (DoctorServiceReview.Type.from(review.getType())) {
                case PIG_DOCTOR:
                    innerDto.setServiceStatus(serviceStatus.getPigdoctorStatus());
                    if (Objects.equals(serviceStatus.getPigdoctorStatus(), DoctorServiceStatus.Status.OPENED.value())) {
                        innerDto.setUrl(this.getPigdoctorUrl(baseUser.getId()));
                    }
                    innerDto.setReason(serviceStatus.getPigdoctorReason());
                    dto.setPigDoctor(innerDto);
                    break;
                case PIGMALL:
                    innerDto.setServiceStatus(serviceStatus.getPigmallStatus());
                    innerDto.setReason(serviceStatus.getPigmallReason());
                    if (Objects.equals(serviceStatus.getPigmallStatus(), DoctorServiceStatus.Status.OPENED.value())) {
                        innerDto.setUrl(pigmallURL);
                    }
                    dto.setPigmall(innerDto);
                    break;
                case NEVEREST:
                    innerDto.setServiceStatus(serviceStatus.getNeverestStatus());
                    innerDto.setReason(serviceStatus.getNeverestReason());
                    dto.setNeverest(innerDto);
                    break;
                // TODO: 18/2/1 暂时取消 
//                case PIG_TRADE:
//                    innerDto.setServiceStatus(serviceStatus.getPigtradeStatus());
//                    innerDto.setReason(serviceStatus.getPigtradeReason());
//                    dto.setPigTrade(innerDto);
//                    break;
            }
        });
        dto.setPigTrade(getPigIot(baseUser));
        dto.setPigJxy(getPigJxy(baseUser, appKey));
        log.info("========userId:{}, dto:{}", baseUser.getId(), dto);
        return dto;
    }

    /**
     * 申请开通服务, 首次申请和驳回后再次申请都可以用这个
     *
     * @param serviceApplyDto 申请信息
     * @return 申请是否成功
     */
    @OpenMethod(key = "apply.open.service", paramNames = "serviceApplyDto")
    public Boolean applyOpenService(@Valid DoctorServiceApplyDto serviceApplyDto) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if (!Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), baseUser.getType())) {
            //只有主账号(猪场管理员)才能申请开通服务
            throw new OPClientException("authorize.fail");
        }
        ServiceBetaStatusToken token = serviceBetaStatusHandler.getServiceBetaStatusToken();
        if (token.inBeta(DoctorServiceReview.Type.from(serviceApplyDto.getType()))) {
            throw new OPClientException("service.in.beta");
        }
        if (serviceApplyDto.getOrg() == null) {
            throw new OPClientException("required.org.info.missing");
        }
        if (StringUtils.isBlank(serviceApplyDto.getOrg().getName())) {
            throw new OPClientException("org.name.not.null");
        }

        Response<DoctorOrg> orgResponse = doctorOrgReadService.findByName(serviceApplyDto.getOrg().getName());
        if (orgResponse.isSuccess() && orgResponse.getResult() != null) {
            throw new OPClientException("org.name.has.existed");
        }

        if (StringUtils.isBlank(serviceApplyDto.getOrg().getLicense())) {
            throw new OPClientException("org.license.not.null");
        }
        if (StringUtils.isBlank(serviceApplyDto.getOrg().getMobile())) {
            throw new OPClientException("org.mobile.not.null");
        }
        return OPRespHelper.orOPEx(doctorServiceReviewService.applyOpenService(baseUser, serviceApplyDto));
    }

    /**
     * 获取用户角色类型
     *
     * @return 角色类型
     * @see io.terminus.doctor.user.enums.RoleType
     */
    @OpenMethod(key = "get.user.role.type")
    public Integer getUserRoleType() {
        return OPRespHelper.orOPEx(doctorUserReadService.findUserRoleTypeByUserId(UserUtil.getUserId()));
    }

    /**
     * 获取用户基本信息
     *
     * @return 用户基本信息
     */
    @OpenMethod(key = "get.user.basic.info")
    public DoctorUserInfoDto getUserBasicInfo() {
        DoctorUserInfoDto doctorUserInfoDto = OPRespHelper.orOPEx(doctorUserReadService.findUserInfoByUserId(UserUtil.getUserId()));
        // 对于子账号, 设置下手机号
        if (Objects.equals(UserType.FARM_SUB.value(), doctorUserInfoDto.getUser().getType()) && doctorUserInfoDto.getUser().getMobile() == null) {
            doctorUserInfoDto.getUser().setMobile(doctorUserInfoDto.getUser().getExtra().get("contact"));
        }
        try {
            Acl acl = aclLoader.getAcl(ParanaThreadVars.getApp());
            BaseUser user = UserUtil.getCurrentUser();
            PermissionData perm = permissionHelper.getPermissions(acl, user, true);
            perm.setAllRequests(null); // empty it
            doctorUserInfoDto.setAuth(ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(perm));
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, JsonResponseException.class);
            log.error("get permissions of user failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new OPClientException("auth.permission.find.fail");
        }
        return doctorUserInfoDto;
    }

    /**
     * 查询用户所在的公司的信息
     *
     * @return 公司id, 公司名称, 营业执照url, 公司手机号
     */
    @OpenMethod(key = "get.org.info")
    public DoctorOrg getOrgInfo() {
        List<DoctorOrg> orgs = OPRespHelper.orOPEx(doctorOrgReadService.findOrgsByUserId(UserUtil.getUserId()));
        // TODO: 2017/2/16 多公司，暂时先返回第一个
        return notEmpty(orgs) ? orgs.get(0) : null;
    }

    /**
     * 查询一级菜单
     *
     * @return
     */
    @OpenMethod(key = "get.user.level.one.menu")
    public List<DoctorMenuDto> getUserLevelOneMenu() {
        return OPRespHelper.orOPEx(doctorMobileMenuReadService.findMenuByUserIdAndLevel(UserUtil.getUserId(), 1));
    }

    @OpenMethod(key = "get.user.by.sessionId", httpMethods = RequestMethod.GET, paramNames = {"sessionId"})
    public User getUserBySessionId(String sessionId) {
        String userInfo = jedisTemplate.execute(jedis -> {
            return jedis.get(redisPrefix + ":" + sessionId);
        });
        if (Strings.isNullOrEmpty(userInfo)) {
            throw new OPClientException("user.not.login");
        }

        Map<String, Object> map = JsonMapperUtil.nonEmptyMapper().fromJson(userInfo, Map.class);
        Integer userId = (Integer) map.get("userId");
        Response<User> userResponse = doctorUserReadService.findById(userId.longValue());
        if (!userResponse.isSuccess() || isNull(userResponse.getResult())) {
            throw new OPClientException("user.not.found");
        }

        return userResponse.getResult();
    }

    private String getPigdoctorUrl(Long userId) {
        //查询关联猪场
        DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
        if (permission != null && permission.getFarmIdsList() != null) {
            //只有一个猪场
            if (permission.getFarmIdsList().size() == 1) {
                return "pigdoctor://pigfarm?homepage_type=2&pig_farm_id=" + permission.getFarmIdsList().get(0);
            }
            //有多个猪场
            if (permission.getFarmIdsList().size() > 1) {
                return farmManageMultiple;
            }
        }
        return null;
    }

    private ServiceReviewOpenDto getPigJxy(BaseUser baseUser, String appKey) {
        ServiceReviewOpenDto openDto = new ServiceReviewOpenDto();

        if (StringUtils.isNotBlank(appKey)) {
            log.info(appKey);
            if (appKey.toUpperCase().contains("IOS") || appKey.toUpperCase().contains("ANDROID")) {
                log.info("is app");
                openDto.setServiceStatus(DoctorServiceStatus.Status.CLOSED.value());
            }

        } else {
            log.info("missing appKey");
            openDto.setServiceStatus(DoctorServiceStatus.Status.BETA.value());
        }


        openDto.setUserId(baseUser.getId());
        openDto.setType(DoctorServiceReview.Type.PIG_JXY.getValue());
        openDto.setStatus(DoctorServiceReview.Status.NOT_OK.getValue());
        openDto.setReason("敬请期待");
//        openDto.setUrl("http://39.108.236.233/app");
        return openDto;
    }

    private ServiceReviewOpenDto getPigIot(BaseUser baseUser) {
        ServiceReviewOpenDto openDto = new ServiceReviewOpenDto();
        openDto.setUserId(baseUser.getId());
        openDto.setType(DoctorServiceReview.Type.PIG_IOT.getValue());
        openDto.setUrl(pigIotUrl);

        //物联运营
        IotUser iotUser = RespHelper.or500(iotUserRoleReadService.findIotUserByUserId(baseUser.getId()));
        if (notNull(iotUser) && Objects.equals(iotUser.getType(), IotUser.TYPE.IOT_ADMIN.getValue())) {
            openDto.setServiceStatus(DoctorServiceStatus.Status.OPENED.value());
            openDto.setStatus(DoctorServiceReview.Status.OK.getValue());
            return openDto;
        }

        if (notNull(iotUser) && Objects.equals(iotUser.getType(), IotUser.TYPE.IOT_OPERATOR.getValue())
                && Objects.equals(iotUser.getStatus(), Sub.Status.ACTIVE.value())) {
            openDto.setServiceStatus(DoctorServiceStatus.Status.OPENED.value());
            openDto.setStatus(DoctorServiceReview.Status.OK.getValue());
            return openDto;
        }

        Response<DoctorUserDataPermission> permissionResponse = doctorUserDataPermissionReadService.findDataPermissionByUserId(baseUser.getId());
        if (!permissionResponse.isSuccess() || isNull(permissionResponse.getResult())
                || Arguments.isNullOrEmpty(permissionResponse.getResult().getFarmIdsList())) {
            openDto.setServiceStatus(DoctorServiceStatus.Status.CLOSED.value());
            openDto.setStatus(DoctorServiceReview.Status.NOT_OK.getValue());
            return openDto;
        }

        DoctorUserDataPermission permission = permissionResponse.getResult();
        List<DoctorFarm> farmList = RespHelper.orServEx(doctorFarmReadService.findFarmsByIds(permission.getFarmIdsList()));
        Boolean isIntelligent = false;
        for (DoctorFarm doctorFarm : farmList) {
            if (Objects.equals(doctorFarm.getIsIntelligent(), IsOrNot.YES.getKey())) {
                isIntelligent = true;
                break;
            }
        }

        if (isIntelligent) {
            openDto.setServiceStatus(DoctorServiceStatus.Status.OPENED.value());
            openDto.setStatus(DoctorServiceReview.Status.OK.getValue());
            return openDto;
        }

        openDto.setServiceStatus(DoctorServiceStatus.Status.BETA.value());//为了让app显示reason，物联网不需要审核
        openDto.setReason("功能未开通");
        openDto.setStatus(DoctorServiceReview.Status.NOT_OK.getValue());
        return openDto;
    }
}
