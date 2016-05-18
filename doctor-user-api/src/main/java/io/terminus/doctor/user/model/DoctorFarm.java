package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪场表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Data
public class DoctorFarm implements Serializable {
    private static final long serialVersionUID = -4931160463332155938L;

    private Long id;
    
    /**
     * 公司名称
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
