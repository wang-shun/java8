package io.terminus.doctor.event.event;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 16/11/9.
 */
@Data
public class ListenedPigEvent implements Serializable {
    private static final long serialVersionUID = 2404642249938824738L;
    private Long pigId;
    private Long pigEventId;
    private Integer eventType;
}
