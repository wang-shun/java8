package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪场职员表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-19
 */
@Data
public class DoctorStaff implements Serializable {
    private static final long serialVersionUID = -403686963673350907L;

    private Long id;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 公司名称
     */
    private String orgName;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 角色id
     */
    private Long roleId;
    
    /**
     * 角色名称(冗余)
     */
    private String roleName;
    
    /**
     * 状态 1:在职，-1:不在职
     */
    private Integer status;
    
    /**
     * 性别 1 男, 2 女
     */
    private Integer sex;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人name
     */
    private String creatorName;
    
    /**
     * 更新人id
     */
    private Long updatorId;
    
    /**
     * 更新人name
     */
    private String updatorName;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
