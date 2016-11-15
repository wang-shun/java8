package io.terminus.doctor.user.manager;

import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.event.EventType;
import io.terminus.doctor.user.interfaces.event.UserEvent;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.parana.user.model.User;
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

    @Transactional
    public void update(UserDto user, String systemCode) throws Exception{
        User paranaUser = BeanMapper.map(user, User.class);
        userDaoExt.update(paranaUser);
        pulishZkEvent(user, EventType.UPDATE, systemCode);
    }

    @Transactional
    public UserDto create(UserDto user, String systemCode) throws Exception {
        User paranaUser = this.makeParanaUserFromInterface(user);
        userDaoExt.create(paranaUser);
        BeanMapper.copy(paranaUser, user);
        pulishZkEvent(user, EventType.CREATE, systemCode);
        return user;
    }

    private User makeParanaUserFromInterface(UserDto user) {
        User paranaUser = BeanMapper.map(user, User.class);
        if(paranaUser.getType() == null){
            paranaUser.setType(UserType.FARM_ADMIN_PRIMARY.value());
        }
        return paranaUser;
    }

    @Transactional
    public void deletes(List<Long> ids, String systemCode) throws Exception {
        if(ids != null){
            userDaoExt.deletes(ids);
            for(Long id : ids){
                pulishZkEvent(new UserDto(id), EventType.DELETE, systemCode);
            }
        }
    }
}
