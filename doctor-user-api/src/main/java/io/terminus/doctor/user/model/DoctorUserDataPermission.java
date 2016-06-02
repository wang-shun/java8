package io.terminus.doctor.user.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Desc: 用户数据权限表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-18
 */
public class DoctorUserDataPermission implements Serializable {
    private static final long serialVersionUID = 8058093995754134945L;

    @Getter @Setter
    private Long id;
    
    /**
     * 用户id
     */
    @Getter @Setter
    private Long userId;
    
    /**
     * 猪场ids, 逗号分隔
     */
    @Getter
    private String farmIds;
    
    /**
     * 猪舍ids, 逗号分隔
     */
    @Getter
    private String barnIds;
    
    /**
     * 仓库类型, 逗号分隔
     */
    @Getter @Setter
    private String wareHouseTypes;
    
    /**
     * 附加字段
     */
    @Getter @Setter
    private String extra;
    
    /**
     * 创建人id
     */
    @Getter @Setter
    private Long creatorId;
    
    /**
     * 创建人name
     */
    @Getter @Setter
    private String creatorName;
    
    /**
     * 更新人id
     */
    @Getter @Setter
    private Long updatorId;
    
    /**
     * 更新人name
     */
    @Getter @Setter
    private String updatorName;
    
    /**
     * 创建时间
     */
    @Getter @Setter
    private Date createdAt;
    
    /**
     * 修改时间
     */
    @Getter @Setter
    private Date updatedAt;

    /**
     * 将 farmIds 转为集合, 方便使用,不存数据库
     */
    @Getter
    private Set<Long> farmIdSet;

    public void setFarmIds(String farmIds){
        this.farmIds = farmIds;
        try{
            this.farmIdSet = Stream.of(farmIds.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        }catch(Exception e){
            this.farmIdSet = new HashSet<>();
        }
    }

    /**
     * 将 barnIds 转为集合, 方便使用,不存数据库
     */
    @Getter
    private Set<Long> barnIdSet;

    public void setBarnIds(String barnIds) {
        this.barnIds = barnIds;
        try{
            this.barnIdSet = Stream.of(barnIds.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        }catch(Exception e){
            this.barnIdSet = new HashSet<>();
        }
    }
}
