package io.terminus.doctor.basic.model.warehouseV2;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-11-06 15:19:42
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseStockHandle implements Serializable {

    private static final long serialVersionUID = -298699325365035201L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 猪厂编号
     */
    private Long farmId;
    
    /**
     * 仓库编号
     */
    private Long warehouseId;
    
    /**
     * 仓库名
     */
    private String warehouseName;
    
    /**
     * 仓库类型
     */
    private Integer warehouseType;
    
    /**
     * 流水号
     */
    private String serialNo;
    
    /**
     * 处理日期
     */
    private Date handleDate;
    
    /**
     * 事件子类型
     */
    private Integer handleSubType;
    
    /**
     * 事件类型
     */
    private Integer handleType;
    
    /**
     * 创建人名
     */
    private String operatorName;
    
    /**
     * 创建人
     */
    private Long operatorId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}