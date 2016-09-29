package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPigSnapShotInfo implements Serializable {
    private static final long serialVersionUID = 1594140745851830048L;

    private DoctorPig pig;
    private DoctorPigTrack pigTrack;
    private DoctorPigEvent pigEvent;
}
