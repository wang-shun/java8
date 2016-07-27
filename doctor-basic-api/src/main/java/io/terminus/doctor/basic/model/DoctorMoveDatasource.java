package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: move-data数据源信息Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-27
 */
@Data
public class DoctorMoveDatasource implements Serializable {
    private static final long serialVersionUID = 7024903827862389187L;

    private Long id;
    
    /**
     * 数据源名称
     */
    private String name;
    
    /**
     * 数据库用户名
     */
    private String username;
    
    /**
     * 数据库密码
     */
    private String password;
    
    /**
     * jdbc driver
     */
    private String driver;
    
    /**
     * 链接url
     */
    private String url;
}
