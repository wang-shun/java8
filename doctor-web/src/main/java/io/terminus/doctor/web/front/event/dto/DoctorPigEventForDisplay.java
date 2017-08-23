package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/5.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPigEventForDisplay extends DoctorPigEvent implements Serializable {
    private static final long serialVersionUID = 6810984617242339516L;
    /**
     * 是否能够回滚事件
     */
    private Boolean isRollback;

    /**
     * 猪当前状态
     */
    private String pigStatus;

    /**
     * 已配种天数
     */
    private Integer matingDay;
}
