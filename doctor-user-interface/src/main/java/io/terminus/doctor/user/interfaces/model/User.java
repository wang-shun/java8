package io.terminus.doctor.user.interfaces.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable{
    private static final long serialVersionUID = -2196001047486486653L;

    private Long id;

    private String name;

    private String email;
    private String mobile;
    private String password;
    private Integer type; //用户类型 1:超级管理员, 2:普通用户, 3:后台运营, 4:站点拥有者
    private Integer status; //用户状态 0:未激活, 1:正常, -1:锁定, -2:冻结, -3: 删除
    private String rolesJson;
    private Date createdAt;
    private Date updatedAt;

}
