package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/10/11.
 * 物联网运营用户与角色管理表
 */
@Data
public class IotUserRole implements Serializable{

    private static final long serialVersionUID = -6956491801378637479L;

    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 物联网角色id
     */
    private Long iotRoleId;

    /**
     * 物联网角色名
     */
    private String iotRoleName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
