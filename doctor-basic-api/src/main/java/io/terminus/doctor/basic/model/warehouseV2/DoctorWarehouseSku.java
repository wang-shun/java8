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
 * Date: 2017-10-26 17:12:43
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseSku implements Serializable {

    private static final long serialVersionUID = -1128431683503011787L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 公司编号
     */
    private Long orgId;

    /**
     * 物料类型编号
     */
    private Long itemId;

    /**
     * 基础物料名称
     */
    private String itemName;

    /**
     * 基础物料类型
     */
    private Integer type;

    /**
     * 物料名称
     */
    private String name;

    /**
     * 编码,用于跨厂调拨
     */
    private String code;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 短码,用于查询
     */
    private String srm;

    /**
     * 供应商编号
     */
    private Long vendorId;

    /**
     * 单位
     */
    private String unit;

    /**
     * 规格
     */
    private String specification;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}