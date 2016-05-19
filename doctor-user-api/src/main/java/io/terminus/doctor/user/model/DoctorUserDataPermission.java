package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 用户数据权限表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-18
 */
@Data
public class DoctorUserDataPermission implements Serializable {
    private static final long serialVersionUID = 8058093995754134945L;

    private Long id;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 猪场ids, 逗号分隔
     */
    private String farmIds;
    
    /**
     * 猪舍ids, 逗号分隔
     */
    private String barnIds;
    
    /**
     * 仓库类型, 逗号分隔
     */
    private String wareHouseTypes;
    
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
