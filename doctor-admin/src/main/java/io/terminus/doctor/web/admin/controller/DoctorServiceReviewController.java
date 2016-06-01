package io.terminus.doctor.web.admin.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.service.*;
import io.terminus.doctor.user.service.business.DoctorServiceReviewService;
import io.terminus.doctor.web.admin.dto.UserApplyServiceDetailDto;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 陈增辉 16/5/30.与用户开通\关闭服务相关的controller
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/service")
public class DoctorServiceReviewController {

    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    private final DoctorServiceReviewService doctorServiceReviewService;
    private final DoctorServiceReviewReadService doctorServiceReviewReadService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorOrgWriteService doctorOrgWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorFarmWriteService doctorFarmWriteService;

    @Autowired
    public DoctorServiceReviewController(DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                                         DoctorServiceReviewService doctorServiceReviewService,
                                         DoctorServiceReviewReadService doctorServiceReviewReadService,
                                         DoctorOrgReadService doctorOrgReadService,
                                         DoctorFarmReadService doctorFarmReadService,
                                         DoctorOrgWriteService doctorOrgWriteService,
                                         DoctorFarmWriteService doctorFarmWriteService){
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorServiceReviewService = doctorServiceReviewService;
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorOrgWriteService = doctorOrgWriteService;
        this.doctorFarmWriteService = doctorFarmWriteService;
    }

    /**
     * 管理员审批允许给用户开通猪场软件服务
     * @return
     */
    @RequestMapping(value = "/pigdoctor/open", method = RequestMethod.POST)
    public Boolean openDoctorService(@RequestBody UserApplyServiceDetailDto dto){
        BaseUser baseUser = UserUtil.getCurrentUser();
        // TODO: 权限中心校验权限

        if (dto.getOrg().getId() == null) {
            throw new JsonResponseException(500, "org.id.can.not.be.null");
        }
        return RespHelper.or500(doctorServiceReviewService.openDoctorService(baseUser, dto.getUserId(), dto.getFarms(), dto.getOrg()));
    }

    /**
     * 开通电商服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @return
     */
    @RequestMapping(value = "/pigmall/open", method = RequestMethod.GET)
    public Boolean openPigmallService(@RequestParam("userId") Long userId){
        BaseUser baseUser = UserUtil.getCurrentUser();
        // TODO: 权限中心校验权限

        //更新服务状态为开通
        return RespHelper.or500(doctorServiceReviewService.openService(baseUser, userId, DoctorServiceReview.Type.PIGMALL));
    }

    /**
     * 开通大数据服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @return
     */
    @RequestMapping(value = "/neverest/open", method = RequestMethod.GET)
    public Boolean openNeverestService(@RequestParam("userId") Long userId){
        BaseUser baseUser = UserUtil.getCurrentUser();
        // TODO: 权限中心校验权限

        //更新服务状态为开通
        return RespHelper.or500(doctorServiceReviewService.openService(baseUser, userId, DoctorServiceReview.Type.NEVEREST));
    }
    /**
     * 管理员审批不允许给用户开通服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @param type 哪一个服务, 参见枚举 DoctorServiceReview.Type
     * @return
     */
    @RequestMapping(value = "/notopen", method = RequestMethod.GET)
    public Boolean notOpenService(@RequestParam("userId") Long userId, @RequestParam("type") Integer type, @RequestParam("reason") String reason){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限

            DoctorServiceReview.Type serviceType = DoctorServiceReview.Type.from(type);
            RespHelper.or500(doctorServiceReviewService.notOpenService(baseUser, userId, serviceType, reason));
        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
        return true;
    }

    /**
     * 管理员冻结用户的服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @param type 哪一个服务, 参见枚举 DoctorServiceReview.Type
     * @param reason 冻结服务的原因
     * @return
     */
    @RequestMapping(value = "/froze", method = RequestMethod.GET)
    public Boolean frozeService(@RequestParam("userId") Long userId, @RequestParam("type") Integer type, @RequestParam("reason") String reason){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限

            DoctorServiceReview.Type serviceType = DoctorServiceReview.Type.from(type);
            RespHelper.or500(doctorServiceReviewService.frozeService(baseUser, userId, serviceType, reason));
        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
        return true;
    }

    /**
     * 分页查询用户提交的申请, 所有参数都可以为空
     * @param userId 申请服务的用户的id, 用于筛选, 不是当前登录者的id
     * @param type 服务类型, 枚举DoctorServiceReview.Type
     * @param status 审核状态 枚举DoctorServiceReview.Status
     * @param pageNo 第几页
     * @param pageSize 每页数量
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Paging<DoctorServiceReview> pageServiceApplies(@RequestParam(value = "userId", required = false) Long userId,
                                     @RequestParam(value = "type", required = false) Integer type,
                                     @RequestParam(value = "status", required = false)Integer status,
                                     @RequestParam Integer pageNo, @RequestParam Integer pageSize){
        try {
            DoctorServiceReview.Type servicetype = null;
            if (type != null) {
                servicetype = DoctorServiceReview.Type.from(type);
            }
            DoctorServiceReview.Status servicecStatus = null;
            if(status != null){
                servicecStatus = DoctorServiceReview.Status.from(status);
            }
            return RespHelper.or500(doctorServiceReviewReadService.page(pageNo, pageSize, userId, servicetype, servicecStatus));
        } catch (ServiceException e) {
            log.error("pageServiceApplies failed, cause : {}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, e.getMessage());
        }
    }

    @RequestMapping(value = "/pigdoctor/detail", method = RequestMethod.GET)
    public UserApplyServiceDetailDto findUserApplyDetail(@RequestParam("userId") Long userId){
        BaseUser baseUser = UserUtil.getCurrentUser();
        // TODO: 权限中心校验权限

        UserApplyServiceDetailDto dto = new UserApplyServiceDetailDto();
        List<String> farms = RespHelper.or500(doctorFarmReadService.findFarmsByUserId(userId)).stream().map(DoctorFarm::getName).collect(Collectors.toList());
        DoctorOrg org = RespHelper.or500(doctorOrgReadService.findOrgByUserId(userId));
        dto.setFarms(farms);
        dto.setUserId(userId);
        dto.setOrg(org);
        return dto;
    }

}
