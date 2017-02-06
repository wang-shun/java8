package io.terminus.doctor.schedule.msg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/1/22.
 * 消息信息封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorMessageInfo implements Serializable{

    private static final long serialVersionUID = 3741679960394645140L;

    private String code;

    /**
     * 母猪胎次
     */
    private Integer parity;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 规则id
     */
    private Long ruleId;

    /**
     * 子账号的角色id
     */
    private Long roleId;

    /**
     * 消息规则模板id
     */
    private Long templateId;

    /**
     * 消息模板的名称
     */
    private String templateName;

    /**
     * 消息数据填充模板的名称
     */
    private String messageTemplate;

    /**
     * 消息类型: 0->系统消息, 1->预警消息, 2->警报消息
     * @see io.terminus.doctor.msg.model.DoctorMessageRuleTemplate.Type
     */
    private Integer type;

    /**
     * 需要操作的事件类型
     * @SeePigEvent
     */
    private Integer eventType;

    /**
     * 消息种类
     */
    private Integer category;

    /**
     * 发送的内容填充数据, json(map). 或系统消息
     */
    private String data;

    /**
     * 产生消息时猪的事件时间(例如:待配种时,断奶事件时间)
     */
    private Date eventAt;

    /**
     * 用于记录其他所需一些事件时间(例如:待配种提示,预产期)
     */
    private Date otherAt;

    /**
     * 事件发生到消息产生时天数与规则中时间时间差
     */
    private Double ruleTimeDiff;

    /**
     * 记录事件发生到消息产生时的时间差
     */
    private Double timeDiff;
    private Long barnId;
    private String barnName;

    /**
     * 消息对象的状态
     */
    private Integer status;
    private String statusName;

    /**
     * 事件的操作人id
     */
    private Long operatorId;

    /**
     * 事件操作人姓名
     */
    private String operatorName;

    /**
     * app回调url
     */
    private String url;
    /**
     * 消息对应的操作id: 猪id、猪群id、物料id
     */
    private Long businessId;

    private Integer businessType;

    private Integer ruleValueId;

    /**
     * 消息产生原因
     */
    private String reason;

}
