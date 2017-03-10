package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群快照表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-25
 */
@Data
public class DoctorGroupSnapshot implements Serializable {
    private static final long serialVersionUID = -6267365953172609599L;

    private Long id;

    /**
     * 操作前的猪群id
     */
    private Long groupId;
    
    /**
     * 操作前的事件id
     */
    private Long fromEventId;
    
    /**
     * 操作后的事件id
     */
    private Long toEventId;


    /**
     * 操作后的信息
     */
    private String toInfo;
    
    /**
     * 创建时间
     */
    private Date createdAt;
}
