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
            Preconditions.checkState(Objects.equals(DoctorServiceReview.Status.INIT.getValue(), review.getStatus())
                            || Objects.equals(DoctorServiceReview.Status.NOT_OK.getValue(), review.getStatus()),
                    "doctor.service.review.status.error");

            //处理数据
            doctorServiceReviewManager.applyOpenService(user, serviceApplyDto.getOrg(), type, serviceApplyDto.getRealName());
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

            response.setResult(true);
        }catch (ServiceException e){
            response.setError(e.getMessage());
        } catch(Exception e){
            log.error("audit service failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("audit.service.failed");
        }
        return response;
    }
}
