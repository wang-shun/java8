package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/10/17.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseMaterialApplyVo {

    //猪舍类别
    private Integer pigType;

    //猪舍名称
    private String pigName;

    //猪群名称
    private String pigGroupName;

    //物料类型
    private Integer skuType;

    //物料名称
    private String skuName;

    //建群日期
    private Date openAt;

    //关群日期
    private Date closeAt;

    //猪场编号
    //private Integer farmId;

    //饲养员
    private String staffName;

    //物料编号
    private String code;

    //数量
    private Double quantity;

    //单价
    private Integer unitPrice;

    //金额
    private Double amount;

    //会计年月
    private Date settlementDate;

    //单位
    private String unit;

    //物料规格
    private String specification;

    //厂家
    private String vendorName;

}
