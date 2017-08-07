package io.terminus.doctor.move.model;

import lombok.Data;

/**
 * Created by xjn on 17/8/4.
 * 猪事件
 */
@Data
public class View_EventListPig {
    /**
     * 猪的性别
     * @see io.terminus.doctor.event.model.DoctorPig.PigSex
     */
    private Integer pigSex;

    private String eventName;       // 事件名称 转换成枚举里需要的值

}
