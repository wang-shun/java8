package io.terminus.doctor.web.admin.controller;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 陈增辉 16/5/30.与用户开通\关闭服务相关的controller
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/service")
public class DoctorServiceReviewController {

    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;

    @Autowired
    public DoctorServiceReviewController(DoctorServiceReviewWriteService doctorServiceReviewWriteService){
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
    }
    /**
     * 管理员审批允许给用户开通服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @param type 哪一个服务, 参见枚举 DoctorServiceReview.Type
     * @return
     */
    @RequestMapping(value = "/open", method = RequestMethod.GET)
    public Boolean openService(@RequestParam("userId") Long userId, @RequestParam("type") Integer type){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限

            DoctorServiceReview.Type serviceType = DoctorServiceReview.Type.from(type);
            RespHelper.or500(doctorServiceReviewWriteService.updateStatus(baseUser, userId, serviceType, DoctorServiceReview.Status.OK));
        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
        return true;
    }

    /**
     * 管理员审批不允许给用户开通服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @param type 哪一个服务, 参见枚举 DoctorServiceReview.Type
     * @return
     */
    @RequestMapping(value = "/notopen", method = RequestMethod.GET)
    public Boolean notOpenService(@RequestParam("userId") Long userId, @RequestParam("type") Integer type){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限

            DoctorServiceReview.Type serviceType = DoctorServiceReview.Type.from(type);
            RespHelper.or500(doctorServiceReviewWriteService.updateStatus(baseUser, userId, serviceType, DoctorServiceReview.Status.NOT_OK));
        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
        return true;
    }

    /**
     * 管理员冻结用户的服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @param type 哪一个服务, 参见枚举 DoctorServiceReview.Type
     * @return
     */
    @RequestMapping(value = "/froze", method = RequestMethod.GET)
    public Boolean frozeService(@RequestParam("userId") Long userId, @RequestParam("type") Integer type){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限

            DoctorServiceReview.Type serviceType = DoctorServiceReview.Type.from(type);
            RespHelper.or500(doctorServiceReviewWriteService.updateStatus(baseUser, userId, serviceType, DoctorServiceReview.Status.FROZEN));
        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
        return true;
    }
}
