package io.terminus.doctor.open.rest.user;

import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.dto.DoctorServiceReviewDto;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;

/**
 * Desc: 用户相关
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@OpenBean
@SuppressWarnings("unused")
public class OPDoctorUsers {

    private final DoctorServiceReviewReadService doctorServiceReviewReadService;

    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;

    private final DoctorUserReadService doctorUserReadService;

    @Autowired
    public OPDoctorUsers(DoctorServiceReviewReadService doctorServiceReviewReadService,
                         DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                         DoctorUserReadService doctorUserReadService) {
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorUserReadService = doctorUserReadService;
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
     * 申请开通服务
     * @param serviceApplyDto 申请信息
     * @return 申请是否成功
     */
    @OpenMethod(key = "apply.open.service", paramNames = "serviceApplyDto")
    public Boolean applyOpenService(@Valid DoctorServiceApplyDto serviceApplyDto) {
        return OPRespHelper.orOPEx(doctorServiceReviewWriteService.applyOpenService(UserUtil.getCurrentUser(), serviceApplyDto));
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
}
