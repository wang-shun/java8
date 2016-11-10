package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xjn on 16/11/9.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListenedGroupEvent implements Serializable{
    private static final long serialVersionUID = 4293105794097951698L;
    private Long groupId;
    private Long orgId;
    private Long farmId;
    private Long doctorGroupEventId;
}
