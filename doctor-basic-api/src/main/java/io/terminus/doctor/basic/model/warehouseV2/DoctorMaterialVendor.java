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
 * Date: 2017-09-11 18:34:04
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialVendor implements Serializable {

    private static final long serialVersionUID = -4509945840156577214L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 供应商名称
     */
    private String vendorName;
    
    /**
     * 仓库编号
     */
    private Long warehouseId;
    
    /**
     * 物料编号
     */
    private Long materialId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}