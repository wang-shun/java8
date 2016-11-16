package io.terminus.doctor.user.interfaces.event;

import io.terminus.doctor.user.interfaces.model.UserDto;
import java.io.Serializable;

public class UserEvent implements Serializable {
    private static final long serialVersionUID = -5258263518867720731L;

    public UserEvent() {}

    public UserEvent(UserDto user, EventType eventType, String systemCode) {
        this.user = user;
        this.eventType = eventType;
        this.systemCode = systemCode;
    }

    public UserEvent(UserDto user, EventType eventType) {
        this.user = user;
        this.eventType = eventType;
        this.systemCode = DoctorSystemCode.PIG_DOCTOR;
    }

    /**
     * 用户信息公共字段
     */
    private UserDto user;


    private EventType eventType;

    /**
     * 每个系统的代号，用于区分不同系统, 主系统是 PigDoctor，
     */
    private String systemCode;

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

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
