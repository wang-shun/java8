package io.terminus.doctor.web.admin.controller;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.business.DoctorServiceReviewService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 陈增辉 16/5/30.与用户开通\关闭服务相关的controller
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/service")
public class DoctorServiceReviewController {

    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    private final DoctorServiceReviewService doctorServiceReviewService;

    @Autowired
    public DoctorServiceReviewController(DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                                         DoctorServiceReviewService doctorServiceReviewService){
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorServiceReviewService = doctorServiceReviewService;
    }

    /**
     * 管理员审批允许给用户开通猪场软件服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @param org 公司信息, 此信息是前台从后台查询得到后再返回给后台
     * @param farms 猪场名称
     * @return
     */
    @RequestMapping(value = "/pigdoctor/open", method = RequestMethod.POST)
    public Boolean openDoctorService(@RequestParam("userId") Long userId, @RequestParam DoctorOrg org, @RequestParam List<String> farms){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限


        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
        return true;
    }

    /**
     * 开通电商读物
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @return
     */
    @RequestMapping(value = "/pigmall/open", method = RequestMethod.GET)
    public Boolean openPigmallService(@RequestParam("userId") Long userId){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限


        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
        return true;
    }

    /**
     * 开通大数据服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @return
     */
    @RequestMapping(value = "/neverest/open", method = RequestMethod.GET)
    public Boolean openNeverestService(@RequestParam("userId") Long userId){
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            // TODO: 权限中心校验权限


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
