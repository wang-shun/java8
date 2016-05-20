package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪舍表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorBarn implements Serializable {
    private static final long serialVersionUID = -4764581389653266924L;

    private Long id;
    
    /**
     * 猪舍名称
     */
    private String name;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 公司名称
     */
    private String orgName;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 猪场名称
     */
    private String farmName;
    
    /**
     * 猪类名称 枚举9种
     */
    private Integer pigType;
    
    /**
     * 能否建群 -1:不能, 1:能
     */
    private Integer canOpenGroup;
    
    /**
     * 使用状态 0:未用 1:在用 -1:已删除
     */
    private Integer status;
    
    /**
     * 猪舍容量
     */
    private Integer capacity;
    
    /**
     * 工作人员id
     */
    private Long staffId;
    
    /**
     * 工作人员name
     */
    private String staffName;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
