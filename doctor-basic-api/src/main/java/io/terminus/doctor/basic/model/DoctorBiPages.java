package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-05 16:04:23
 * Created by [ your name ]
 */
@Data
public class DoctorBiPages implements Serializable {

    private static final long serialVersionUID = -403144309860082823L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 页面名称
     */
    private String name;
    
    /**
     * 
     */
    private String token;
    
    /**
     * BI页面地址
     */
    private String url;
    
    /**
     * 描述说明
     */
    private String desc;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}