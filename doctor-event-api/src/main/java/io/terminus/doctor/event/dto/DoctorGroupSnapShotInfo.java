package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.Data;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc: 猪群快照信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
@Builder
public class DoctorGroupSnapShotInfo implements Serializable {
    private static final long serialVersionUID = 3463240858656522252L;

    private DoctorGroup group;

    private DoctorGroupEvent groupEvent;

    private DoctorGroupTrack groupTrack;
}
