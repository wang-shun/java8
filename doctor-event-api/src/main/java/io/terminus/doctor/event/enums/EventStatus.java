package io.terminus.doctor.event.enums;

import lombok.Getter;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 09:36 17/3/9
 */

public enum EventStatus {
    NORMAL(1, "有效"),
    HANDLING(0, "处理中"),
    DISABLED(-1, "无效");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    EventStatus(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static EventStatus from(Integer key){
        for(EventStatus eventStatus: EventStatus.values()){
            if(eventStatus.getKey() == key){
                return eventStatus;
            }
        }
        return null;
    }
}
