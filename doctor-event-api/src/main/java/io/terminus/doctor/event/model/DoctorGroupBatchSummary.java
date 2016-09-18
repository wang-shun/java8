package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群批次总结表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */
@Data
public class DoctorGroupBatchSummary implements Serializable {
    private static final long serialVersionUID = -2290422361878542260L;

    private Long id;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 猪群id
     */
    private Long groupId;
    
    /**
     * 猪群号
     */
    private String groupCode;

    /**
     * 猪群状态
     * @see io.terminus.doctor.event.model.DoctorGroup.Status
     */
    private Integer status;
    
    /**
     * 猪类
     */
    private Integer pigType;
    
    /**
     * 平均日龄
     */
    private Integer avgDayAge;
    
    /**
     * 建群时间
     */
    private Date openAt;
    
    /**
     * 关群时间
     */
    private Date closeAt;
    
    /**
     * 猪舍id
     */
    private Long barnId;
    
    /**
     * 猪舍名称
     */
    private String barnName;
    
    /**
     * 工作人员id
     */
    private Long userId;
    
    /**
     * 工作人员name
     */
    private String userName;
    
    /**
     * 窝数
     */
    private Integer nest;
    
    /**
     * 活仔数
     */
    private Integer liveCount;
    
    /**
     * 健仔数
     */
    private Integer healthCount;
    
    /**
     * 弱仔数
     */
    private Integer weakCount;
    
    /**
     * 出生成本(分)
     */
    private Long birthCost;
    
    /**
     * 出生均重(kg)
     */
    private Double birthAvgWeight;
    
    /**
     * 死淘率
     */
    private Double deadRate;
    
    /**
     * 断奶数
     */
    private Integer weanCount;
    
    /**
     * 不合格数
     */
    private Integer unqCount;
    
    /**
     * 断奶均重(kg)
     */
    private Double weanAvgWeight;
    
    /**
     * 销售头数
     */
    private Integer saleCount;
    
    /**
     * 销售金额(分)
     */
    private Long saleAmount;
    
    /**
     * 转保育成本(分)
     */
    private Long toNurseryCost;
    
    /**
     * 转保育数量
     */
    private Integer toNurseryCount;
    
    /**
     * 转保育均重(kg)
     */
    private Double toNurseryAvgWeight;
    
    /**
     * 转育肥成本(分)
     */
    private Long toFattenCost;
    
    /**
     * 转育肥数量
     */
    private Integer toFattenCount;
    
    /**
     * 转育肥均重(kg)
     */
    private Double toFattenAvgWeight;
    
    /**
     * 转入数
     */
    private Integer inCount;
    
    /**
     * 转入均重(kg)
     */
    private Double inAvgWeight;
    
    /**
     * 转入成本(均)
     */
    private Long inCost;
    
    /**
     * 料肉比
     */
    private Double fcr;
    
    /**
     * 出栏成本(分)
     */
    private Long outCost;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
