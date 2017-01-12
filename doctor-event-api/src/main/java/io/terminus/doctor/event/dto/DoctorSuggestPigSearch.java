package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorPig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/1/12.
 * 封装查询pigTrack的查询条件
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSuggestPigSearch implements Serializable{
    private static final long serialVersionUID = -8720788699359419021L;

    private Long farmId;

    /**
     * 用于模糊搜索
     */
    private String pigCode;
    /**
     * 猪类型信息表数据
     *
     * @see DoctorPig.PigSex
     */
    private Integer sex;

    /**
     * @see io.terminus.doctor.event.enums.PigStatus
     */
    private Integer status;

    private List<Integer> statuses;

    private List<Integer> barnTypes;

    /**
     * 不包含的状态
     */
    private Integer notStatus;

    /**
     * 不包含的配种次数
     */
    private Integer notMatingCount;

    /**
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    private Integer isRemoval;

}
