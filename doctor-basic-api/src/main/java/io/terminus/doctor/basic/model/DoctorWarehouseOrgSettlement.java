package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 16:23:41
 * Created by [ your name ]
 */
@Data
public class DoctorWarehouseOrgSettlement implements Serializable {

    private static final long serialVersionUID = 7695321450791443677L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 上一次结算的会计年月
     */
    private Date lastSettlementDate;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}