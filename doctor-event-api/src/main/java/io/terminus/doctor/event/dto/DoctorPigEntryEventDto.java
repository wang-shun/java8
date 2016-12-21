package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import lombok.Data;

/**
 * Created by yudi on 2016/12/21.
 * Mail to yd@terminus.io
 */
@Data
public class DoctorPigEntryEventDto {
    private DoctorBasicInputInfoDto doctorBasicInputInfoDto;
    private DoctorFarmEntryDto doctorFarmEntryDto;
}
