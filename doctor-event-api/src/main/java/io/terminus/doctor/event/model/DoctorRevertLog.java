package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 回滚记录表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorRevertLog implements Serializable {
    private static final long serialVersionUID = 1861449639048681648L;

    private Long id;
    
    /**
     * 回滚类型 1 母猪，2 公猪，3 猪群
     */
    private Integer type;
    
    /**
     * 回滚前的信息
     */
    private String fromInfo;
    
    /**
     * 回滚后的信息
     */
    private String toInfo;
    
    /**
     * 回滚人id
     */
    private Long reverterId;
    
    /**
     * 回滚人姓名
     */
    private String reverterName;
    
    /**
     * 回滚时间
     */
    private Date createdAt;
}
