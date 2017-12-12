package io.terminus.doctor.event.dto;

import io.terminus.doctor.common.utils.JsonMapperUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatisticCriteria implements Serializable {
    private static final long serialVersionUID = -2306055017759941831L;

    public DoctorStatisticCriteria(Long farmId, Integer pigType, String sumAt) {
        this.farmId = farmId;
        this.pigType = pigType;
        this.sumAt = sumAt;
    }

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 猪群类型(猪群报表时可选)
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;

    /**
     * 统计某一天 = 格式：yyyy-MM-dd
     */
    private String sumAt;

    /**
     * 开始时间 >= 格式：yyyy-MM-dd
     */
    private String startAt;

    /**
     * 结束时间 <= 格式：yyyy-MM-dd
     */
    private String endAt;

    public Map<String, Object> toMap() {
        return JsonMapperUtil.nonEmptyMapper().getMapper().convertValue(this, Map.class);
    }
}
