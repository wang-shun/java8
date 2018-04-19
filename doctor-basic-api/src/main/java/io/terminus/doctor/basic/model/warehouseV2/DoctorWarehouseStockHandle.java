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
 * Date: 2018-04-18 23:12:59
 * Created by [ your name ]
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWarehouseStockHandle implements Serializable {

    private static final long serialVersionUID = 5800380886917720051L;

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
     * 流水号
     */
    private String serialNo;

    /**
     * 处理日期
     */
    private Date handleDate;

    /**
     * 事件子类型
     *
     * @see io.terminus.doctor.basic.enums.WarehouseMaterialHandleType
     */
    private Integer handleSubType;

    /**
     * 事件类型
     *
     * @see io.terminus.doctor.basic.enums.WarehouseMaterialHandleType
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
     * 仓库类型
     */
    private Integer warehouseType;

    /**
     * 关联单据id
     * 用于关联配方生产出库单据和配方生产入库单据
     * 用于关联调拨出库和调拨入库单据
     */
    private Long relStockHandleId;

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