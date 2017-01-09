package io.terminus.doctor.event.event;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪事件EventBus携带信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/01/09
 */
@Data
public class ListenedPigEvent implements Serializable {
    private static final long serialVersionUID = 2404642249938824738L;

    private Long orgId;

    private Long farmId;

    private List<DoctorPigPublishDto> pigs;
}
