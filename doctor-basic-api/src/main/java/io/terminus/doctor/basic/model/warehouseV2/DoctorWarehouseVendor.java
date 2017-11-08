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
 * Date: 2017-10-30 18:00:26
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseVendor implements Serializable {

    private static final long serialVersionUID = 5143884748269242977L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 供应商名称
     */
    private String name;
    
    /**
     * 简称
     */
    private String shortName;
    
    /**
     * 删除标志，0正常，1删除
     * @see io.terminus.doctor.basic.enums.WarehouseVendorDeleteFlag
     */
    private Integer deleteFlag;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}