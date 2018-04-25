package io.terminus.doctor.basic.model.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/10/17.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseMaterialApplyPigGroup implements Serializable {

    private static final long serialVersionUID = 2900698176964489592L;

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

    //猪场名称
    private String farmName;

    //猪群id
    private Integer pigGroupId;

    //物料id
    private Integer skuId;

    private Integer pigType;
}
