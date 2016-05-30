package io.terminus.doctor.msg.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息规则与角色表Model类
 * Date: 2016-05-30
 * author: chk@terminus.io
 */
@Data
public class DoctorMessageRuleRole implements Serializable {
    private static final long serialVersionUID = 1227408724682903349L;

    private Long id;
    
    /**
     * 子账号的角色id
     */
    private Long roleId;
    
    /**
     * 预警类型id
     */
    private Long ruleId;
    
    /**
     * 消息类型, doctor_message_rules表冗余
     */
    private Integer type;
    
    /**
     * 预警规则类型, doctor_message_rules表冗余
     */
    private Integer ruleType;
    
    /**
     * 预警值
     */
    private Long ruleValue;
    
    /**
     * 预警开始值
     */
    private Long ruleStartValue;
    
    /**
     * 预警结束值
     */
    private Long ruleEndValue;
    
    /**
     * 消息发送渠道, 多个以逗号分隔. 0-&gt;站内信, 1-&gt;短信, 2-&gt;邮箱
     */
    private String channel;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
}
