package io.terminus.doctor.basic.model.warehouseV2;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-12 13:58:42
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseStockHandle implements Serializable {

    private static final long serialVersionUID = -551430614143675093L;

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
     * 流水号
     */
    private String serialNo;
    
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