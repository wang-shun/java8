package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪只数统计表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */
@Data
public class DoctorPigTypeStatistic implements Serializable {
    private static final long serialVersionUID = 3053973085443889095L;

    private Long id;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 公猪数
     */
    private Integer boar;
    
    /**
     * 母猪数
     */
    private Integer sow;
    
    /**
     * 产房仔猪数
     */
    private Integer farrow;
    
    /**
     * 保育猪数
     */
    private Integer nursery;
    
    /**
     * 育肥猪数
     */
    private Integer fatten;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
