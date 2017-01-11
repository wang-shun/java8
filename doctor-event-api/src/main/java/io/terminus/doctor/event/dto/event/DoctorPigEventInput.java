package io.terminus.doctor.event.dto.event;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/1/5.
 */
@Data
public class DoctorPigEventInput implements Serializable {
    private static final long serialVersionUID = -9212749161236663656L;

    private DoctorBasicInputInfoDto basic;

    private BasePigEventInputDto inputDto;
}
