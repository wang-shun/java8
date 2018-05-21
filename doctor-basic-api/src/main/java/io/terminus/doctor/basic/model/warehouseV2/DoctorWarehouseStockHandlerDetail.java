package io.terminus.doctor.basic.model.warehouseV2;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-17 11:48:43
 * Created by [ your name ]
 */
@Data
@Deprecated
public class DoctorWarehouseStockHandlerDetail implements Serializable {

    private static final long serialVersionUID = -4299065974836791083L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 库存处理编号
     */
    private Long handlerId;

    /**
     * 转入仓库编号
     */
    private Long targetWarehouseId;

    /**
     * 出库到的猪舍编号
     */
    private Long pigId;

    /**
     * 出库到的猪舍名称
     */
    private String pigName;

    /**
     * 库存编号
     */
    private Long stockId;

    /**
     * 数量
     */
    private java.math.BigDecimal number;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单价，单位分
     */
    private Long unitPrice;

    /**
     * 领用人编号
     */
    private Long recipientId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}