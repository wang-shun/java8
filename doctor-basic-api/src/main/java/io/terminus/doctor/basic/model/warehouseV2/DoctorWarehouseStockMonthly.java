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
 * Date: 2017-12-10 15:21:37
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class DoctorWarehouseStockMonthly implements Serializable {

    private static final long serialVersionUID = -5611630923930815284L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 仓库编号
     */
    private Long warehouseId;
    
    /**
     * 物料编号
     */
    private Long materialId;
    
    /**
     * 处理年
     */
    private Integer handleYear;
    
    /**
     * 处理月
     */
    private Integer handleMonth;
    
    /**
     * 余量
     */
    private java.math.BigDecimal balanceQuantity;
    
    /**
     * 余额
     */
    private Long balacneAmount;
    
    /**
     * 处理日期
     */
    private Date handleDate;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}