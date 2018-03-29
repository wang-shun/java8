package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-29 10:49:19
 * Created by [ your name ]
 */
@Data
public class DoctorDemo implements Serializable {

    private static final long serialVersionUID = 7236251607902467536L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}