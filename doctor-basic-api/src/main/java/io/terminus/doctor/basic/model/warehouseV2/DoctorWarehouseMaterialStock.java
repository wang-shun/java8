package io.terminus.doctor.basic.model.warehouseV2;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-23 15:46:02
 * Created by [ your name ]
 */
@Data
@Deprecated
public class DoctorWarehouseMaterialStock implements Serializable {

    private static final long serialVersionUID = -8038121428294626667L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 
     */
    private Long warehouseId;
    
    /**
     * 
     */
    private String warehouseName;
    
    /**
     * 
     */
    private Integer warehouseType;
    
    /**
     * 
     */
    private Long farmId;
    
    /**
     * 
     */
    private Long materialId;
    
    /**
     * 
     */
    private String materialName;
    
    /**
     * 
     */
    private java.math.BigDecimal quantity;
    
    /**
     * 
     */
    private String unit;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}