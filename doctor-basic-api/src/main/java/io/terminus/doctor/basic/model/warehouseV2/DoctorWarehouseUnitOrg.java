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
 * Date: 2017-10-30 16:08:20
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseUnitOrg implements Serializable {

    private static final long serialVersionUID = -7323825264736822656L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 公司编号
     */
    private Long orgId;
    
    /**
     * 单位编号
     */
    private Long unitId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}