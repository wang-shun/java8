package io.terminus.doctor.event.dto.search;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.enums.PigStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Desc: 猪舍ElasticSearch查询结果
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
public class SearchedBarn implements Serializable {
    private static final long serialVersionUID = -7833906381116273417L;

    private Long id;

    /**
     * 猪舍名
     */
    private String name;

    /**
     * 猪场
     */
    private Long farmId;

    private String farmName;

    /**
     * 猪类名称 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;

    private String pigTypeName;

    /**
     * 能否建群 -1:不能, 1:能
     */
    private Integer canOpenGroup;

    /**
     * 使用状态 0:未用 1:在用 -1:已删除
     */
    private Integer status;

    /**
     * 查询详情的类型
     * @see io.terminus.doctor.common.enums.PigSearchType
     */
    private Integer type;

    /**
     * 存栏量
     */
    private Integer storage;

    /**
     * 猪群数量
     */
    private Integer pigGroupCount;

    /**
     * 猪数量
     */
    private Integer pigCount;

    /**
     * 猪舍容量
     */
    private Integer capacity;

    /**
     * 工作人员
     */
    private Long staffId;

    private String staffName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 聚合猪舍里猪的各种状态
     * @see BarnStatus
     */
    private List<BarnStatus> barnStatuses;


    /**
     * 猪舍里各种状态的猪
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BarnStatus implements Serializable{
        private static final long serialVersionUID = 7792826158693919446L;

        private Integer count;
        private String name;
        private String desc;
    }

    //公猪母猪状态
    public static BarnStatus createPigStatus(PigStatus pigStatus, Integer count) {
        if (pigStatus == null) {
            return null;
        }
        return BarnStatus.builder()
                .count(count)
                .name(pigStatus.getName())
                .desc(pigStatus.getDesc())
                .build();
    }

    //猪群类别
    public static BarnStatus createGroupStatus(PigType pigType, Integer count) {
        if (pigType == null) {
            return null;
        }
        return BarnStatus.builder()
                .count(count)
                .name(pigType.getDesc())
                .desc(pigType.getDesc())
                .build();
    }

    //产房仔猪类别
    public static BarnStatus createFarrowStatus(Integer count) {
        return BarnStatus.builder().count(count).name("产房仔猪").desc("产房仔猪").build();
    }
}
