package io.terminus.doctor.user.service.business;

import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.DoctorFarm;
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

    /**
     * 给用户开通猪场软件服务
     * @param user 登录者
     * @param userId 被操作的用户
     * @param farms 猪场名称
     * @return
     */
    Response<List<DoctorFarm>> openDoctorService(BaseUser user, Long userId, String loginName, DoctorOrg org, List<DoctorFarm> farms);

    /**
     * 开通服务, 更新服务状态并保存状态变更历史记录
     * @param user 登录者
     * @param userId 被操作的用户
     * @param type 服务类型
     * @return
     */
    Response<Boolean> openService(BaseUser user, Long userId, DoctorServiceReview.Type type);

    /**
     * 不开通服务, 更新服务状态并保存状态变更历史记录
     * @param user 登录者
     * @param userId 被操作的用户
     * @param type 服务类型
     * @return
     */
    Response<Boolean> notOpenService(BaseUser user, Long userId, DoctorServiceReview.Type type, String reason);

    /**
     * 冻结申请服务的资格, 冻结后就不能申请了
     * @param user 登录者
     * @param userId 被操作的用户
     * @param type 服务类型
     * @return
     */
    Response<Boolean> frozeApply(BaseUser user, Long userId, DoctorServiceReview.Type type, String reason);

}
