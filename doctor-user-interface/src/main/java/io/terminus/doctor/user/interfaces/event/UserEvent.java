package io.terminus.doctor.user.interfaces.event;

import io.terminus.doctor.user.interfaces.model.UserDto;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/11/15.
 */
public class UserEvent implements Serializable {
    private static final long serialVersionUID = -5258263518867720731L;

    public UserEvent(UserDto user, EventType eventType){
        this.user = user;
        this.eventType = eventType;
    }

    private UserDto user;
    private EventType eventType;

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
