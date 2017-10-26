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
 * Date: 2017-10-26 16:16:34
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseVendorOrg implements Serializable {

    private static final long serialVersionUID = 7765516154498848967L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 公司编号
     */
    private Long orgId;
    
    /**
     * 供应商编号
     */
    private Long vendorId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}