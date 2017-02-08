package io.terminus.doctor.event.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/8.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEventOperator implements Serializable{
    private static final long serialVersionUID = 7222583109278471675L;

    /**
     * 事件操作人, 不一定是录入者
     */
    private Long operatorId;
    /**
     * 事件操作人, 不一定是录入者
     */
    private String operatorName;
}
