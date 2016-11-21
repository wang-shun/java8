package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪场基础数据关联表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
@Data
public class DoctorFarmBasic implements Serializable {
    private static final long serialVersionUID = 7093649893981275019L;

    private Long id;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 基础数据ids, 逗号分隔
     */
    private String basicIds;
    
    /**
     * 变动原因ids, 逗号分隔
     */
    private String reasonIds;
    
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
