package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群卡片明细表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorGroupTrack implements Serializable {
    private static final long serialVersionUID = -423032174027191008L;

    private Long id;
    
    /**
     * 猪群卡片id
     */
    private Long groupId;
    
    /**
     * 关联的最新一次的事件id
     */
    private Long relEventId;
    
    /**
     * 猪只数
     */
    private Integer quantity;

    /**
     * 公猪数
     */
    private Integer boarQty;

    /**
     * 母猪数
     */
    private Integer sowQty;

    /**
     * 出生日期(此日期仅用于计算日龄)
     */
    private Date birthDate;

    /**
     * 平均日龄
     */
    private Integer avgDayAge;
    
    /**
     * 总活体重(公斤)
     */
    private Double weight;
    
    /**
     * 平均体重(公斤)
     */
    private Double avgWeight;
    
    /**
     * 单价(分)
     */
    private Long price;
    
    /**
     * 总金额(分)
     */
    private Long amount;
    
    /**
     * 客户id
     */
    private Long customerId;
    
    /**
     * 客户名称
     */
    private String customerName;
    
    /**
     * 销售数量
     */
    private Integer saleQty;
    
    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人name
     */
    private String creatorName;
    
    /**
     * 更新人id
     */
    private Long updatorId;
    
    /**
     * 更新人name
     */
    private String updatorName;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
