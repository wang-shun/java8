package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-14 12:30:22
 * Created by [ your name ]
 */
@Data
public class DoctorFiledUrl implements Serializable {

    private static final long serialVersionUID = 6882603220595773203L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 字段名称
     */
    private String name;
    
    /**
     * 字段跳转url
     */
    private String url;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}