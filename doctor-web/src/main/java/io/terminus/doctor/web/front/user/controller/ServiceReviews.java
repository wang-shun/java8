package io.terminus.doctor.web.front.user.controller;

import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.dto.DoctorServiceReviewDto;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.business.DoctorServiceReviewService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/user/service")
public class ServiceReviews {
    private final DoctorServiceReviewReadService doctorServiceReviewReadService;

    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    private final DoctorServiceReviewService doctorServiceReviewService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorUserReadService doctorUserReadService;

    @Autowired
    public ServiceReviews(DoctorServiceReviewReadService doctorServiceReviewReadService,
                         DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                         DoctorUserReadService doctorUserReadService,
                         DoctorServiceReviewService doctorServiceReviewService,
                         DoctorOrgReadService doctorOrgReadService) {
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceReviewService = doctorServiceReviewService;
        this.doctorOrgReadService = doctorOrgReadService;
    }

    @RequestMapping(value = "/getUserServiceStatus", method = RequestMethod.GET)
    @ResponseBody
    public DoctorServiceReviewDto getUserServiceStatus() {
        return RespHelper.or500(doctorServiceReviewReadService.findServiceReviewDtoByUserId(UserUtil.getUserId()));
    }

    @RequestMapping(value = "/applyOpenService", method = RequestMethod.POST)
    @ResponseBody
    public Boolean applyOpenService(@Valid @RequestBody DoctorServiceApplyDto serviceApplyDto) {
        return RespHelper.or500(doctorServiceReviewService.applyOpenService(UserUtil.getCurrentUser(), serviceApplyDto));
    }

    /**
     * 获取用户角色类型
     * @return 角色类型
     * @see io.terminus.doctor.user.enums.RoleType
     */
    @RequestMapping(value = "/getUserRoleType", method = RequestMethod.GET)
    public Integer getUserRoleType() {
        return RespHelper.or500(doctorUserReadService.findUserRoleTypeByUserId(UserUtil.getUserId()));
    }

    /**
     * 获取用户基本信息
     * @return 用户基本信息
     */
    @RequestMapping(value = "/getUserBasicInfo", method = RequestMethod.GET)
    public DoctorUserInfoDto getUserBasicInfo() {
        return null;
    }

    /**
     * 查询用户所在的公司的信息
     * @return 公司id, 公司名称, 营业执照url, 公司手机号
     */
    @RequestMapping(value = "/getOrgInfo", method = RequestMethod.GET)
    public DoctorOrg getOrgInfo() {
        return RespHelper.or500(doctorOrgReadService.findOrgByUserId(UserUtil.getUserId()));
    }
}
