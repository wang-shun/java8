package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 前台猪群详情Dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorGroupDetailEventsDto implements Serializable {
    private static final long serialVersionUID = -1311702577458524040L;

    /**
     * 猪群
     */
    private DoctorGroup group;

    /**
     * 猪群跟踪
     */
    private DoctorGroupTrack groupTrack;

    /**
     * 猪群事件s
     */
    private List<DoctorGroupEvent> groupEvents;

    private Long canRollback;
}
