package io.terminus.doctor.basic.model.warehouseV2;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-11 11:35:21
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseStockHandler implements Serializable {

    private static final long serialVersionUID = -29827459968091193L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 处理类型。入库，出库，盘点，调拨
     */
    private Integer handlerType;
    
    /**
     * 处理日期
     */
    private Date handlerDate;
    
    /**
     * 仓库编号
     */
    private Long warehouseId;
    
    /**
     * 猪长编号
     */
    private Long farmId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}