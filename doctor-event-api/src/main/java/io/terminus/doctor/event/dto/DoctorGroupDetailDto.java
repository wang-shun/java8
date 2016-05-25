package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc: 猪群详情
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorGroupDetailDto implements Serializable {
    private static final long serialVersionUID = 1431382048910172923L;

    /**
     * 猪群
     */
    private DoctorGroup group;

    /**
     * 猪群跟踪
     */
    private DoctorGroupTrack groupTrack;
}
