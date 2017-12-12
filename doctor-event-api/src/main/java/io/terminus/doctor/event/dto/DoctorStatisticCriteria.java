package io.terminus.doctor.event.dto;

import io.terminus.doctor.common.utils.JsonMapperUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 */
@Data
public class DoctorStatisticCriteria implements Serializable {
    private static final long serialVersionUID = -2306055017759941831L;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 猪群类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;

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
