package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪事件EventBus携带信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/01/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListenedPigEvent implements Serializable {
    private static final long serialVersionUID = 2404642249938824738L;

    private Long orgId;

    private Long farmId;

    /**
     * 猪事件类型
     * @see io.terminus.doctor.event.enums.PigEvent
     */
    private Integer eventType;

    private List<DoctorPigPublishDto> pigs;
}
