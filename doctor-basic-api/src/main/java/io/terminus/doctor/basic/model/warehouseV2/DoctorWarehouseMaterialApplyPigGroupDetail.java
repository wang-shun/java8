package io.terminus.doctor.basic.model.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseMaterialApplyPigGroupDetail implements Serializable {

    private static final long serialVersionUID = -1544032127028041983L;

    //物料名称
    private String skuName;

    //物料类型
    private Integer skuType;

    //仓库名称
    private String warehouseName;

    //时间日期
    private Date applyDate;

    //会计年月
    private Date settlementDate;

    //事件类型
    private Integer type;

    //数量
    private Double quantity;

    //单价
    private Integer unitPrice;

    //金额
    private Double amount;

    //猪舍名
    private String pigBarnName;

    //猪舍类型
    private Integer pigType;

    //猪群名称
    private String pigGroupName;

    //饲养员
    private String staffName;

    //猪场名称
    private String farmName;

    //单位
    private String unit;

    //厂家
    private String vendorName;

    //规格
    private String specification;
}
