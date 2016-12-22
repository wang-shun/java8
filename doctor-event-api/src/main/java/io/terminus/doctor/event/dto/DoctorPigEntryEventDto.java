package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by yudi on 2016/12/21.
 * Mail to yd@terminus.io
 */
@Data
public class DoctorPigEntryEventDto implements Serializable{
    private static final long serialVersionUID = -7177561185907847241L;

    private DoctorBasicInputInfoDto doctorBasicInputInfoDto;
    private DoctorFarmEntryDto doctorFarmEntryDto;
}
