package io.terminus.doctor.open.listener;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.web.core.events.user.RegisterEvent;
import io.terminus.doctor.web.core.service.ServiceBetaStatusService;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * open层的用户事件监听器 16/6/12.
 * @author 陈增辉
 */
@Slf4j
@Component
public class OpenUserEventListener implements EventListener {
    private final DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    private final DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    private final UserReadService<User> userReadService;
    private final ServiceBetaStatusService serviceBetaStatusService;

    @Autowired
    public OpenUserEventListener(DoctorServiceStatusWriteService doctorServiceStatusWriteService,
                                 DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                                 UserReadService<User> userReadService,
                                 ServiceBetaStatusService serviceBetaStatusService){
        this.doctorServiceStatusWriteService = doctorServiceStatusWriteService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.userReadService = userReadService;
        this.serviceBetaStatusService = serviceBetaStatusService;
    }

    @Subscribe
    public void onUserRegister(RegisterEvent registerEvent){
        ParanaUser paranaUser = registerEvent.getUser();
        if(paranaUser == null || paranaUser.getId() == null){
            log.error("catch user register event, but parameter ParanaUser or ParanaUserId is null.");
            return;
        }
        Long userId = paranaUser.getId();
        User user = RespHelper.orServEx(userReadService.findById(userId));

        //当注册用户是猪场管理员(主账号)
        if(Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), paranaUser.getType())){
            serviceBetaStatusService.initDefaultServiceStatus(userId);
            doctorServiceReviewWriteService.initServiceReview(userId, user.getMobile());
        }
    }
}
