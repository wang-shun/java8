package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorBarn;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪舍搜索查询
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/1/3
 */
@Data
public class DoctorBarnDto extends DoctorBarn implements Serializable {
    private static final long serialVersionUID = 3885978965992758468L;

    private List<Integer> pigTypes;

    /**
     * 权限过滤的猪舍id
     */
    private List<Long> barnIds;
}
