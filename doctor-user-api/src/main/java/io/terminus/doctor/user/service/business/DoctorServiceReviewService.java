package io.terminus.doctor.user.service.business;

import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;

import java.util.List;

/**
 * 陈增辉16/5/30.
 * 用户开通\关闭\冻结服务相关
 * 用于处理复杂业务逻辑, 带有事务控制
 */
public interface DoctorServiceReviewService {

    /**
     * 申请开通服务
     * @param baseUser  当前登录用户
     * @param serviceApplyDto   申请信息
     * @return 是否成功
     */
    Response<Boolean> applyOpenService(BaseUser baseUser, DoctorServiceApplyDto serviceApplyDto);

    Response<Boolean> openDoctorService(BaseUser user, Long userId, List<String> farms, DoctorOrg org);
}
