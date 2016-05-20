package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群事件表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorGroupEvent implements Serializable {
    private static final long serialVersionUID = 2651236908562482893L;

    private Long id;
    
    /**
     * 公司id
     */
    private String orgId;
    
    /**
     * 公司名称
     */
    private String orgName;
    
    /**
     * 猪场id
     */
    private String farmId;
    
    /**
     * 猪场名称
     */
    private String farmName;
    
    /**
     * 猪群卡片id
     */
    private String groupId;
    
    /**
     * 猪群号
     */
    private String groupCode;
    
    /**
     * 事件发生日期
     */
    private Date eventAt;
    
    /**
     * 事件类型 枚举 总共10种
     */
    private Integer type;
    
    /**
     * 事件名称 冗余枚举的name
     */
    private String name;
    
    /**
     * 事件描述
     */
    private String desc;
    
    /**
     * 事件发生猪舍id
     */
    private Long barnId;
    
    /**
     * 事件发生猪舍name
     */
    private String barnName;
    
    /**
     * 猪类枚举 9种
     */
    private Integer pigType;
    
    /**
     * 事件猪只数
     */
    private Integer quantity;
    
    /**
     * 总活体重(公斤)
     */
    private Double weight;
    
    /**
     * 平均体重(公斤)
     */
    private Double avgWeight;
    
    /**
     * 平均日龄
     */
    private Double avgDayAge;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 具体事件的内容通过json存储
     */
    private String extra;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人name
     */
    private String creatorName;
}
