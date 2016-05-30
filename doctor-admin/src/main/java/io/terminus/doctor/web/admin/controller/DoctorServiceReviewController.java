package io.terminus.doctor.web.admin.controller;

import com.google.common.base.Preconditions;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 陈增辉 16/5/30.与用户开通\关闭服务相关的controller
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/service")
public class DoctorServiceReviewController {

    private final
    /**
     * 管理员审批是否允许给用户开通服务
     * @param userId 被操作的用户的id, 注意不是当前登录者的id
     * @param type 哪一个服务, 参见枚举 DoctorServiceReview.Type
     * @param status 审核结果为开通还是不开通, 1:审核通过,允许开通, -1:审核不通过,不允许开通
     * @return
     */
    @RequestMapping(value = "/open", method = RequestMethod.GET)
    public Boolean openService(@RequestParam("userId") Long userId, @RequestParam("type") Integer type, @RequestParam("status") Integer status){
        BaseUser baseUser = UserUtil.getCurrentUser();
        // TODO: 权限中心校验权限

        DoctorServiceReview.Type serviceType = DoctorServiceReview.Type.from(type);
        Preconditions.checkArgument(serviceType != null, "doctor.service.review.type.error");
        Preconditions.checkArgument(Objects.equals(DoctorServiceReview.Status.OK.getValue(), status)
                || Objects.equals(DoctorServiceReview.Status.NOT_OK.getValue(), status),
                "doctor.service.review.status.error");
        return true;
    }
}
