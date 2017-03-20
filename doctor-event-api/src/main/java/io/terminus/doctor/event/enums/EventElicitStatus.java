package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 14:33 17/3/10
 */


public enum EventElicitStatus {
    EDIT(1, "编辑"),
    ADD(2, "新增"),
    DELETE(3, "删除");

    @Getter
    private Integer value;

    @Getter
    private String desc;

    EventElicitStatus(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static EventElicitStatus from(Integer value){
        for(EventElicitStatus eventElicitStatus: EventElicitStatus.values()){
            if(Objects.equals(eventElicitStatus.getValue(), value)){
                return eventElicitStatus;
            }
        }
        return null;
    }
}
