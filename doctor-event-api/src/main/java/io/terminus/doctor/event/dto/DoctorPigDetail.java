package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigDetail implements Serializable {
    private static final long serialVersionUID = 8922354799293966452L;

    private DoctorPig pig;
    private DoctorPigTrack pigTrack;
}
