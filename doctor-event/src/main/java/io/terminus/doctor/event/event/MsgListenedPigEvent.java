package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪事件zk携带信息
 * xjn
 * Date: 2017/01/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsgListenedPigEvent implements Serializable {
    private static final long serialVersionUID = 2404642249938824738L;

    private Long orgId;

    private Long farmId;

    private List<MsgPigPublishDto> pigs;
}
