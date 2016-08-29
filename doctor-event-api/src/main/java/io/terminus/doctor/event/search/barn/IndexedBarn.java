package io.terminus.doctor.event.search.barn;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪舍ElasticSearch索引
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
public class IndexedBarn implements Serializable {
    private static final long serialVersionUID = 8495272694652815993L;
    private Long id;

    /**
     * 猪舍名
     */
    private String name;
    // 搜索词
    private String nameSearch;


    /**
     * 公司
     */
    private Long orgId;
    private String orgName;

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
     * 存栏量
     */
    private Integer storage;

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
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 猪舍排序
     */
    private Integer order;

}
