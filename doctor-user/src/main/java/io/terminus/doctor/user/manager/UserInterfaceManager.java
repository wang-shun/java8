package io.terminus.doctor.user.manager;

import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.event.EventType;
import io.terminus.doctor.user.interfaces.event.UserEvent;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by chenzenghui on 16/11/15.
 */
@Slf4j
@Component
public class UserInterfaceManager {

    private final Publisher publisher;

    @Autowired
    private UserDaoExt userDaoExt;
    @Autowired
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @Autowired
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    @Autowired
    private UserWriteService<User> userUserWriteService;

    @Autowired
    public UserInterfaceManager(ZKClientFactory zkClientFactory, @Value("${user.center.topic}") String userCenterTopic) throws Exception {
        this.publisher = new Publisher(zkClientFactory, userCenterTopic);
    }

    private void pulishZkEvent(UserDto user, EventType eventType, String systemCode) throws Exception {
        try {
            publisher.publish(JsonMapper.nonEmptyMapper().toJson(new UserEvent(user, eventType, systemCode)).getBytes());
        } catch(Exception e) {
            log.info("throw a error when publish event, user:{}, eventType:{}, systemCode:{}", user, eventType.name(), systemCode);
            if(!e.getMessage().equals("no subscribers exists")){
                throw e;
            }
        }
    }

    /**
     * 向同一zkTopic下的各个子系统广播，由各子系统处理此次更新事件
     */
    @Transactional
    public void update(UserDto user, String systemCode) throws Exception{
        User paranaUser = BeanMapper.map(user, User.class);
        userDaoExt.update(paranaUser);
        pulishZkEvent(user, EventType.UPDATE, systemCode);
    }

    /**
     * 向同一zkTopic下的各个子系统广播，由各子系统处理此次创建事件
     */
    @Transactional
    public UserDto create(UserDto user, String systemCode) throws Exception {
        registerByMobile(BeanMapper.map(user, User.class), systemCode);
        pulishZkEvent(user, EventType.CREATE, systemCode);
        return user;
    }

    /**
     * 向同一zkTopic下的各个子系统广播，由各子系统处理此次删除事件
     */
    @Transactional
    public void deletes(List<Long> ids, String systemCode) throws Exception {
        if(ids != null){
            userDaoExt.deletes(ids);
            for(Long id : ids){
                pulishZkEvent(new UserDto(id), EventType.DELETE, systemCode);
            }
        }
    }

    //猪场都走手机号
    private User registerByMobile(User user, String systemCode) {
        checkMobileRepeat(user.getMobile());

        //猪场用户所需的字段
        user.setStatus(UserStatus.NORMAL.value());  //默认正常
        user.setType(UserType.FARM_ADMIN_PRIMARY.value()); //默认猪场主账号
        user.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));
        user.setId(RespHelper.orServEx(userUserWriteService.create(user)));

        //初始化审核信息
        initReview(user, systemCode);
        return user;
    }

    //初始化审核信息， // TODO: 2016/11/15 以后根据 systemCode 设置各个系统的状态
    private void initReview(User user, String systemCode) {
        DoctorServiceStatus status = new DoctorServiceStatus();
        status.setUserId(user.getId());

        status.setPigdoctorStatus(DoctorServiceStatus.Status.CLOSED.value());
        status.setPigdoctorReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        status.setPigmallStatus(DoctorServiceStatus.Status.BETA.value());
        status.setPigmallReviewStatus(DoctorServiceReview.Status.INIT.getValue());
        status.setPigmallReason("敬请期待");

        status.setNeverestStatus(DoctorServiceStatus.Status.BETA.value());
        status.setNeverestReviewStatus(DoctorServiceReview.Status.INIT.getValue());
        status.setNeverestReason("敬请期待");

        status.setPigtradeStatus(DoctorServiceStatus.Status.BETA.value());
        status.setPigtradeReviewStatus(DoctorServiceReview.Status.INIT.getValue());
        status.setPigtradeReason("敬请期待");

        doctorServiceStatusWriteService.createServiceStatus(status);
        doctorServiceReviewWriteService.initServiceReview(user.getId(), user.getMobile());
    }

    // 检测手机号是否已存在
    private void checkMobileRepeat(String mobile) {
        if(userDaoExt.findByMobile(mobile) != null){
            throw new ServiceException("user.register.mobile.has.been.used");
        }
    }
}
