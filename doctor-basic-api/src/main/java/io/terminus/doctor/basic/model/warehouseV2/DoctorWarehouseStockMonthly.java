package io.terminus.doctor.basic.model.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-19 17:05:11
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseStockMonthly implements Serializable {

    private static final long serialVersionUID = -3569557538659802255L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 公司id
     */
    private Long orgId;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 仓库编号
     */
    private Long warehouseId;

    /**
     * 物料编号
     */
    private Long materialId;

    /**
     * 余量
     */
    private java.math.BigDecimal balanceQuantity;

    /**
     * 余额
     */
    private java.math.BigDecimal balanceAmount;

    /**
     * 会计年月
     */
    private Date settlementDate;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}