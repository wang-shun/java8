package io.terminus.doctor.open.listener;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.web.core.events.user.RegisterEvent;
import io.terminus.doctor.web.core.service.ServiceBetaStatusHandler;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.zookeeper.pubsub.Publisher;
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
    private final ServiceBetaStatusHandler serviceBetaStatusHandler;
    //用于通过zookeeper向pigmall电商系统分发事件
    private final Publisher publish2Pigmall;

    @Autowired
    public OpenUserEventListener(DoctorServiceStatusWriteService doctorServiceStatusWriteService,
                                 DoctorServiceReviewWriteService doctorServiceReviewWriteService,
                                 UserReadService<User> userReadService,
                                 ServiceBetaStatusHandler serviceBetaStatusHandler,
                                 Publisher publish2Pigmall) {
        this.doctorServiceStatusWriteService = doctorServiceStatusWriteService;
        this.doctorServiceReviewWriteService = doctorServiceReviewWriteService;
        this.userReadService = userReadService;
        this.serviceBetaStatusHandler = serviceBetaStatusHandler;
        this.publish2Pigmall = publish2Pigmall;
    }

    @Subscribe
    public void onUserRegister(RegisterEvent registerEvent){
        ParanaUser paranaUser = registerEvent.getUser();
        if(paranaUser == null || paranaUser.getId() == null){
            log.error("catch user register event, but parameter ParanaUser or ParanaUserId is null.");
            return;
        }
        User user = RespHelper.orServEx(userReadService.findById(paranaUser.getId()));

        //当注册用户是猪场管理员(主账号)
        if(Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), paranaUser.getType())){
            serviceBetaStatusHandler.initDefaultServiceStatus(paranaUser.getId());
            doctorServiceReviewWriteService.initServiceReview(paranaUser.getId(), user.getMobile());
        }
    }
}
