package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.Data;

import java.io.Serializable;

/**
 * 母猪拼窝需要的数据
 * Created by highway on 16/8/9.
 */
@Data
public class DoctorFosterDetail implements Serializable {
    private static final long serialVersionUID = -557596844974270121L;

    DoctorGroupTrack doctorGroupTrack;

    DoctorPigInfoDto doctorPigInfoDto;
}
