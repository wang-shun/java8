package io.terminus.doctor.user.model;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
     * 公司Id，逗号分隔
     */
    @Getter
    private String orgIds;
    /**
     * 集团Id，用逗号隔开（孔景军）
     */
    @Getter
    private String groupIds;
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
     * 将 groupIds 转为集合, 方便使用,不存数据库（孔景军）
     */

    @Getter
    private List<Long> groupIdsList;

    public void setGroupIds(String groupIds){
        this.groupIds = groupIds;
        if (StringUtils.isNotBlank(groupIds)) {
            this.groupIdsList = Splitters.COMMA.splitToList(groupIds).stream().map(Long::valueOf).collect(Collectors.toList());
        } else {
            this.groupIdsList = Lists.newArrayList();
        }

    }

    /**
     * 将 farmIds 转为集合, 方便使用,不存数据库
     */
    @Getter
    private List<Long> farmIdsList;

    public void setFarmIds(String farmIds){
        this.farmIds = farmIds;
        if (StringUtils.isNotBlank(farmIds)) {
            this.farmIdsList = Splitters.COMMA.splitToList(farmIds).stream().map(Long::valueOf).collect(Collectors.toList());
        } else {
            this.farmIdsList = Lists.newArrayList();
        }

    }

    /**
     * 将 barnIds 转为集合, 方便使用,不存数据库
     */
    @Getter
    private List<Long> barnIdsList;

    public void setBarnIds(String barnIds) {
        this.barnIds = barnIds;
        if (StringUtils.isNotBlank(barnIds)) {
            this.barnIdsList = Splitters.COMMA.splitToList(barnIds).stream().map(Long::valueOf).collect(Collectors.toList());
        } else {
            this.barnIdsList = Lists.newArrayList();
        }
    }
    /**
     * 将 orgIds 转为集合, 方便使用,不存数据库
     */
    @Getter
    private List<Long> orgIdsList = new ArrayList<>();

    public void setOrgIds(String orgIds) {
        this.orgIds = orgIds;
        if (StringUtils.isNotBlank(orgIds)) {
            this.orgIdsList = Splitters.COMMA.splitToList(orgIds).stream().map(Long::valueOf).collect(Collectors.toList());
        } else {
            this.orgIdsList = Lists.newArrayList();
        }
    }

    public void setOrgIdsList(List<Long> orgIdsList){
        this.orgIdsList = orgIdsList == null ? new ArrayList<>() : orgIdsList;
        if(orgIdsList != null && !orgIdsList.isEmpty()){
            this.orgIds = Joiner.on(",").join(orgIdsList);
        }else{
            this.orgIds = null;
        }
    }

    public void setGroupIdsList(List<Long> groupIdsList){
        this.groupIdsList = groupIdsList == null ? new ArrayList<>() : groupIdsList;
        if(groupIdsList != null && !groupIdsList.isEmpty()){
            this.groupIds = Joiner.on(",").join(groupIdsList);
        }else{
            this.groupIds = null;
        }
    }
}
