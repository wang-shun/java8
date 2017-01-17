package io.terminus.doctor.basic.model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Desc: 猪场基础数据关联表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
@Data
public class DoctorFarmBasic implements Serializable {
    private static final long serialVersionUID = 7093649893981275019L;

    private Long id;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 基础数据ids, 逗号分隔
     */
    private String basicIds;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Long> basicIdList;
    
    /**
     * 变动原因ids, 逗号分隔
     */
    private String reasonIds;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Long> reasonIdList;

    private String materialIds;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Long> materialIdList;

    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;

    @SneakyThrows
    public List<Long> getBasicIdList() {
        return Strings.isNullOrEmpty(basicIds) ?
                Lists.newArrayList() : Splitters.splitToLong(basicIds, Splitters.COMMA);
    }

    @SneakyThrows
    public List<Long> getReasonIdList() {
        return Strings.isNullOrEmpty(reasonIds) ?
                Lists.newArrayList() : Splitters.splitToLong(reasonIds, Splitters.COMMA);
    }

    @SneakyThrows
    public List<Long> getMaterialIdList() {
        return Strings.isNullOrEmpty(materialIds) ?
                Lists.newArrayList() : Splitters.splitToLong(materialIds, Splitters.COMMA);
    }
}
