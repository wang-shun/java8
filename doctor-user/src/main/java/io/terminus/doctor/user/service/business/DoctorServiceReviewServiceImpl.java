package io.terminus.doctor.user.service.business;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.manager.DoctorServiceReviewManager;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 陈增辉16/5/30.
 * 用户开通\关闭\冻结服务相关
 * 用于处理复杂业务逻辑, 带有事务控制
 */
@Slf4j
@Service
public class DoctorServiceReviewServiceImpl implements DoctorServiceReviewService{

    private final DoctorServiceReviewDao doctorServiceReviewDao;
    private final DoctorServiceReviewManager doctorServiceReviewManager;

    @Autowired
    public DoctorServiceReviewServiceImpl(DoctorServiceReviewDao doctorServiceReviewDao,
                                          DoctorServiceReviewManager doctorServiceReviewManager){
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.doctorServiceReviewManager = doctorServiceReviewManager;
    }

    @Override
    public Response<Boolean> applyOpenService(BaseUser user, DoctorServiceApplyDto serviceApplyDto) {
        Response<Boolean> response = new Response<>();
        try {
            //检验枚举
            DoctorServiceReview.Type type = DoctorServiceReview.Type.from(serviceApplyDto.getType());
            Preconditions.checkArgument(type != null, "doctor.service.review.type.error");

            //查询, 校验数据库
            DoctorServiceReview  review = doctorServiceReviewDao.findByUserIdAndType(user.getId(), type);

            //已被冻结申请资格
            Preconditions.checkState(!Objects.equals(DoctorServiceReview.Status.FROZEN.getValue(), review.getStatus()), "user.service.frozen");
            //状态不是初始化或驳回
            Preconditions.checkState(Objects.equals(DoctorServiceReview.Status.INIT.getValue(), review.getStatus())
                            || Objects.equals(DoctorServiceReview.Status.NOT_OK.getValue(), review.getStatus()),
                    "doctor.service.review.status.error");

            //处理数据
            doctorServiceReviewManager.applyOpenService(user, serviceApplyDto.getOrg(), type, review);
            response.setResult(true);
        } catch (ServiceException | IllegalStateException | IllegalArgumentException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("applyOpenService failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("apply.open.service.failed");
        }
        return response;
    }
    @Override
    public Response<Boolean> openDoctorService(BaseUser user, Long userId, List<String> farms, DoctorOrg org){
        Response<Boolean> response = new Response<>();
        try{
            DoctorServiceReview review = doctorServiceReviewDao.findByUserIdAndType(userId, DoctorServiceReview.Type.PIG_DOCTOR);
            Preconditions.checkState(Objects.equals(DoctorServiceReview.Status.REVIEW.getValue(), review.getStatus()), "user.service.not.applied");
            doctorServiceReviewManager.openDoctorService(user, userId, farms, org);
            response.setResult(true);
        }catch (ServiceException | IllegalStateException e){
            response.setError(e.getMessage());
        } catch(Exception e){
            log.error("open service failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("audit.service.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> openService(BaseUser user, Long userId, DoctorServiceReview.Type type){
        Response<Boolean> response = new Response<>();
        try {
            DoctorServiceReview review = doctorServiceReviewDao.findByUserIdAndType(userId, type);
            if (!Objects.equals(review.getStatus(), DoctorServiceReview.Status.REVIEW.getValue())) {
                return Response.fail("user.service.not.applied");
            }
            doctorServiceReviewManager.updateServiceReviewStatus(user, userId, type, DoctorServiceReview.Status.REVIEW, DoctorServiceReview.Status.OK, null);
            response.setResult(true);
        } catch (Exception e) {
            log.error("update doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> notOpenService(BaseUser user, Long userId, DoctorServiceReview.Type type, String reason) {
        Response<Boolean> response = new Response<>();
        try {
            DoctorServiceReview review = doctorServiceReviewDao.findByUserIdAndType(userId, type);
            if (!Objects.equals(review.getStatus(), DoctorServiceReview.Status.REVIEW.getValue())) {
                return Response.fail("user.service.not.applied");
            }
            doctorServiceReviewManager.updateServiceReviewStatus(user, userId, type, DoctorServiceReview.Status.REVIEW, DoctorServiceReview.Status.NOT_OK, reason);
            response.setResult(true);
        } catch (Exception e) {
            log.error("update doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> frozeApply(BaseUser user, Long userId, DoctorServiceReview.Type type, String reason) {
        Response<Boolean> response = new Response<>();
        try {
            DoctorServiceReview review = doctorServiceReviewDao.findByUserIdAndType(userId, type);
            if (!Objects.equals(review.getStatus(), DoctorServiceReview.Status.REVIEW.getValue())) {
                return Response.fail("user.service.not.applied");
            }
            doctorServiceReviewManager.updateServiceReviewStatus(user, userId, type, DoctorServiceReview.Status.from(review.getStatus()),
                    DoctorServiceReview.Status.FROZEN, reason);
            response.setResult(true);
        } catch (Exception e) {
            log.error("update doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.doctor.service.review.failed");
        }
        return response;
    }
}
