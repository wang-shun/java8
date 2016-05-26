package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群快照表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorGroupSnapshot implements Serializable {
    private static final long serialVersionUID = -8594460676067820159L;

    private Long id;
    
    /**
     * 操作前的猪群id
     */
    private Long fromGroupId;
    
    /**
     * 操作后的猪群id
     */
    private Long toGroupId;
    
    /**
     * 操作前的事件id
     */
    private Long fromEventId;
    
    /**
     * 操作后的事件id
     */
    private Long toEventId;
    
    /**
     * 操作前的信息
     */
    private String fromInfo;
    
    /**
     * 操作后的信息
     */
    private String toInfo;
    
    /**
     * 创建时间
     */
    private Date createdAt;
}
