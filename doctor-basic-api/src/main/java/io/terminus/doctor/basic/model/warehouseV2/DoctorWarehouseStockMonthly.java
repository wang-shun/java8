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
 * Date: 2017-09-29 13:22:37
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseStockMonthly implements Serializable {

    private static final long serialVersionUID = 260174866031166018L;

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
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}