package io.terminus.doctor.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-01 17:01:25
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorTrackSnapshot implements Serializable {

    private static final long serialVersionUID = 708682492571659666L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 猪场名称
     */
    private String farmName;
    
    /**
     * 猪或者猪群id
     */
    private Long businessId;
    
    /**
     * 猪或者猪群Code
     */
    private String businessCode;
    
    /**
     * 类型，1-》猪，2-》猪群
     */
    private Integer businessType;
    
    /**
     * 前置事件或者编辑记录id
     */
    private Long eventId;
    
    /**
     * track json
     */
    private String trackJson;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}