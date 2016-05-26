package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 陈增辉16/5/19.
 * 用户在其他系统的账户绑定关系
 */
@Data
public class UserBind implements Serializable{
    private static final long serialVersionUID = 6154481492567863024L;

    private Long id;

    private Long userId;

    private Integer targetSystem; // 关联枚举 TargetSystem

    private String uuid;

    private String targetUserName;

    private String targetUserMobile;

    private String targetUserEmail;

    private String extra;

    private Date createdAt;

    private Date updatedAt;
}
