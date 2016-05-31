package io.terminus.doctor.open.rest.user;

import com.google.common.collect.Lists;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.dto.DoctorMenuDto;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.dto.DoctorServiceReviewDto;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.business.DoctorServiceReviewService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.parana.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.util.List;

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
    private final DoctorServiceReviewService doctorServiceReviewService;

    private final DoctorUserReadService doctorUserReadService;

    @Autowired
    public OPDoctorUsers(DoctorServiceReviewReadService doctorServiceReviewReadService,
                         DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                         DoctorUserReadService doctorUserReadService,
                         DoctorServiceReviewService doctorServiceReviewService) {
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceReviewService = doctorServiceReviewService;
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
        return OPRespHelper.orOPEx(doctorServiceReviewService.applyOpenService(UserUtil.getCurrentUser(), serviceApplyDto));
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
        return new DoctorUserInfoDto(mockUser(), getUser().getType() % 4, 1L, mockStaff(getUser().getId()));
    }

    private DoctorUser getUser() {
        return UserUtil.getCurrentUser();
    }

    private User mockUser() {
        User user = new User();
        user.setId(getUser().getId());
        user.setName(getUser().getName());
        user.setMobile(getUser().getMobile());
        user.setStatus(1);
        user.setType(2);
        return user;
    }

    private DoctorStaff mockStaff(Long userId) {
        DoctorStaff staff = new DoctorStaff();
        staff.setId(userId);
        staff.setOrgId(userId);
        staff.setOrgName("测试公司"+userId);
        staff.setUserId(userId);
        staff.setRoleId(1L);
        staff.setRoleName("仓库管理员");
        staff.setStatus(1);
        staff.setSex(1);
        staff.setAvatar("http://img.xrnm.com/20150821-ee59df0636a3291405b61f997d314a19.jpg");
        return staff;
    }

    @OpenMethod(key = "get.user.level.one.menu")
    public List<DoctorMenuDto> getUserLevelOneMenu() {
        return Lists.newArrayList(mockMenuDto(1L), mockMenuDto(2L), mockMenuDto(3L));
    }

    private DoctorMenuDto mockMenuDto(Long id) {
        DoctorMenuDto menuDto = new DoctorMenuDto();
        menuDto.setId(id);
        menuDto.setName("menu" + id);
        menuDto.setLevel(1);
        menuDto.setUrl("/user/info");
        menuDto.setHasIcon(0);
        menuDto.setType(3);
        menuDto.setOrderNo(id.intValue());
        menuDto.setNeedHiden(0);
        return menuDto;
    }
}
