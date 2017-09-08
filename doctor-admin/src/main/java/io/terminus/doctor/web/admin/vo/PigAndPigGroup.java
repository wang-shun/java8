package io.terminus.doctor.web.admin.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/9/7.
 */
@Data
public class PigAndPigGroup {


    /**
     * 状态
     */
    private Integer status;


    /**
     * 胎次
     */
    private Integer currentParity;


    /**
     * 品种
     */
    private Long breedId;

    /**
     * 品系
     */
    private Long geneticId;


    /**
     * 舍号
     */
    private Long currentBarnId;


    /**
     * 是否离场
     */
    private Integer isRemoval;


    /**
     * 分娩数量
     */
    private Integer farrowQty;

    /**
     * 未断奶数量
     */
    private Integer unweanQty;

    /**
     * 分娩均重(kg)
     */
    private Double farrowAvgWeight;

    /**
     * 断奶数量
     */
    private Integer weanQty;

    /**
     * 断奶均重(kg)
     */
    private Double weanAvgWeight;


    /**
     * 公猪类型
     */
    private Integer boarType;

    /**
     * 猪群中猪的数量
     */
    private Integer quantity;

    /**
     * 平均日龄
     */
    private Integer avgDayAge;

    /**
     * 猪群建群时间
     */
    private Date openAt;


    /**
     * 活仔数(分娩时累加)
     */
    private Integer liveQty;

    /**
     * 健仔数(分娩时累加)
     */
    private Integer healthyQty;

    /**
     * 弱仔数
     */
    private Integer weakQty;

    @NotNull(message = "god.pig.and.group.type.null")
    private Integer type;

}
