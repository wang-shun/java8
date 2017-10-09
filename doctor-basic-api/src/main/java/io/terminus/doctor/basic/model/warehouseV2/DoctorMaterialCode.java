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
 * Date: 2017-09-11 17:13:42
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialCode implements Serializable {

    private static final long serialVersionUID = 5877687992085861862L;

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
     * 供应商名
     */
    private String vendorName;
    
    /**
     * 规格
     */
    private String specification;
    
    /**
     * 编码
     */
    private String code;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}