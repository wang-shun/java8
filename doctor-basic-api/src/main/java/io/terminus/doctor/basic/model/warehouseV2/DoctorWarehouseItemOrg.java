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
 * Date: 2017-11-02 22:15:38
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseItemOrg implements Serializable {

    private static final long serialVersionUID = 7166858308436444349L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 物料类目编号
     */
    private Long itemId;
    
    /**
     * 公司编号
     */
    private Long orgId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}