package io.terminus.doctor.schedule.user;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.service.*;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 陈增辉 on 16/6/13.用户服务状态初始化job
 * 用户注册时有一个事件分发,在事件监听器中已经初始化了用户的服务状态,此job只是再做一次数据检查和处理,理论上不应该真的会有数据需要处理
 */
@Slf4j
@EnableScheduling
@Component
public class UserServiceInit {
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorServiceReviewReadService doctorServiceReviewReadService;
    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    private final DoctorServiceStatusReadService doctorServiceStatusReadService;
    private final DoctorServiceStatusWriteService doctorServiceStatusWriteService;

    @Autowired
    public UserServiceInit(DoctorUserReadService doctorUserReadService,
                           DoctorServiceReviewReadService doctorServiceReviewReadService,
                           DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                           DoctorServiceStatusReadService doctorServiceStatusReadService,
                           DoctorServiceStatusWriteService doctorServiceStatusWriteService){
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceReviewReadService = doctorServiceReviewReadService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.doctorServiceStatusReadService = doctorServiceStatusReadService;
        this.doctorServiceStatusWriteService = doctorServiceStatusWriteService;
    }

    @Scheduled(cron = "0 */15 * * * ?")
    public void userServiceInit() {
        try{
            for (User user : doctorUserReadService.listCreatedUserSince(DateTime.now().minusMinutes(15).toDate()).getResult()){
                if(!Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), user.getType())){
                    continue;
                }
                Long userId = user.getId();
                Response<List<DoctorServiceReview>> reviewRes = doctorServiceReviewReadService.findServiceReviewsByUserId(userId);
                if(reviewRes.isSuccess() && reviewRes.getResult().isEmpty()){
                    doctorServiceReviewWriteService.initServiceReview(userId, user.getMobile());
                }
                Response<DoctorServiceStatus> statusResponse = doctorServiceStatusReadService.findByUserId(userId);
                if(statusResponse.isSuccess() && statusResponse.getResult() == null){
                    doctorServiceStatusWriteService.initDefaultServiceStatus(userId);
                }
            }
        }catch(Exception e){
            log.error("userServiceInit job error:{}", Throwables.getStackTraceAsString(e));
        }
    }
}
