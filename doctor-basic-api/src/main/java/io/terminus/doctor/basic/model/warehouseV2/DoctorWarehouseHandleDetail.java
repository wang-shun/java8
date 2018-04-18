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
 * Date: 2017-09-06 13:16:45
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class DoctorWarehouseHandleDetail implements Serializable {

    private static final long serialVersionUID = -1939299962983172278L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 物料采购记录编号
     */
    private Long materialPurchaseId;
    
    /**
     * 物料处理记录编号
     */
    private Long materialHandleId;
    
    /**
     * 
     */
    private Integer handleYear;
    
    /**
     * 
     */
    private Integer handleMonth;
    
    /**
     * 处理数量
     */
    private java.math.BigDecimal quantity;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}