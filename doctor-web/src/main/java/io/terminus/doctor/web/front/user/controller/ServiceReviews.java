package io.terminus.doctor.web.front.user.controller;

import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserType;
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
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/user/service")
public class ServiceReviews {
    private final DoctorServiceReviewReadService doctorServiceReviewReadService;
    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    private final DoctorServiceReviewService doctorServiceReviewService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorServiceStatusReadService doctorServiceStatusReadService;
    private final PrimaryUserReadService primaryUserReadService;
    private final ServiceBetaStatusHandler serviceBetaStatusHandler;
    private final UserReadService<User> userReadService;

    @Autowired
    public ServiceReviews(DoctorServiceReviewReadService doctorServiceReviewReadService,
                          DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                          DoctorUserReadService doctorUserReadService,
                          DoctorServiceReviewService doctorServiceReviewService,
                          DoctorOrgReadService doctorOrgReadService,
                          DoctorServiceStatusReadService doctorServiceStatusReadService,
                          PrimaryUserReadService primaryUserReadService,
                          ServiceBetaStatusHandler serviceBetaStatusHandler,
                          UserReadService<User> userReadService) {
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceReviewService = doctorServiceReviewService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorServiceStatusReadService = doctorServiceStatusReadService;
        this.primaryUserReadService = primaryUserReadService;
        this.serviceBetaStatusHandler = serviceBetaStatusHandler;
        this.userReadService = userReadService;
    }

    /**
     * 根据用户id查询 用户服务开通情况
     * @return 服务开通情况
     */
    @RequestMapping(value = "/getUserServiceStatus", method = RequestMethod.GET)
    @ResponseBody
    public DoctorServiceStatus getUserServiceStatus() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        Long primaryUserId; //主账号id

        if(Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), baseUser.getType())){
            //当前用户是主账号,则直接查询
            primaryUserId = baseUser.getId();
        }else if(Objects.equals(UserType.FARM_SUB.value(), baseUser.getType())){
            //当前用户是子账号, 找他的主账号
            primaryUserId = RespHelper.or500(primaryUserReadService.findSubByUserId(baseUser.getId())).getParentUserId();
        }else{
            throw new JsonResponseException("authorize.fail");
        }
        return RespHelper.or500(doctorServiceStatusReadService.findByUserId(primaryUserId));
    }

    /**
     * 申请开通服务, 首次申请和驳回后再次申请都可以用这个
     * @param serviceApplyDto 申请信息
     * @return 申请是否成功
     */
    @RequestMapping(value = "/applyOpenService", method = RequestMethod.POST)
    @ResponseBody
    public Boolean applyOpenService(@Valid @RequestBody DoctorServiceApplyDto serviceApplyDto) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(!Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), baseUser.getType())){
            //只有主账号(猪场管理员)才能申请开通服务
            throw new JsonResponseException("authorize.fail");
        }
        ServiceBetaStatusToken dto = serviceBetaStatusHandler.getServiceBetaStatusToken();
        if(dto.inBeta(DoctorServiceReview.Type.from(serviceApplyDto.getType()))){
            throw new JsonResponseException("service.in.beta");
        }
        return RespHelper.or500(doctorServiceReviewService.applyOpenService(baseUser, serviceApplyDto));
    }

    /**
     * 获取用户角色类型
     * @return 角色类型
     * @see io.terminus.doctor.user.enums.RoleType
     */
    @RequestMapping(value = "/getUserRoleType", method = RequestMethod.GET)
    @ResponseBody
    public Integer getUserRoleType() {
        return RespHelper.or500(doctorUserReadService.findUserRoleTypeByUserId(UserUtil.getUserId()));
    }

    /**
     * 获取用户基本信息
     * @return 用户基本信息
     */
    @RequestMapping(value = "/getUserBasicInfo", method = RequestMethod.GET)
    @ResponseBody
    public DoctorUserInfoDto getUserBasicInfo() {
        return RespHelper.or500(doctorUserReadService.findUserInfoByUserId(UserUtil.getUserId()));
    }

    /**
     * 查询用户所在的公司的信息
     * @return 公司id, 公司名称, 营业执照url, 公司手机号
     */
    @RequestMapping(value = "/getOrgInfo", method = RequestMethod.GET)
    @ResponseBody
    public DoctorOrg getOrgInfo() {
        return RespHelper.or500(doctorOrgReadService.findOrgByUserId(UserUtil.getUserId()));
    }

    @RequestMapping(value = "/getUserLevelOneMenu", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorMenuDto> getUserLevelOneMenu() {
        return Lists.newArrayList();
    }

}
