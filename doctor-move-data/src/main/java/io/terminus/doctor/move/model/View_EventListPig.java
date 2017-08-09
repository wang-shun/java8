package io.terminus.doctor.move.model;

import lombok.Data;

import java.util.Date;

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

    private String pigCode;

    private String pigOutId;

    /**
     * 事件名称 转换成枚举里需要的值
     * @see io.terminus.doctor.event.enums.PigEvent
     */
    private String eventName;

    private Date eventAt;
    private String eventDesc;

    private String barnOutId;       // 事件发生猪舍
    private String toBarnOutId;     // 进场事件: 进场猪舍outId, 转舍的目的地


}
