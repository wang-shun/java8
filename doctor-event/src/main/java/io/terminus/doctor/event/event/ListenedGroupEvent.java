package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪群事件EventBus携带信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/11/30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListenedGroupEvent implements Serializable{
    private static final long serialVersionUID = 4293105794097951698L;
    private Long orgId;

    private Long farmId;

    private List<DoctorGroupPublishDto> groups;
}
