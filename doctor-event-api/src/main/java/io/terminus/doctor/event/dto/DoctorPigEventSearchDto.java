package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by xiao on 16/9/13.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorPigEventSearchDto extends DoctorPigEvent implements Serializable {

    private static final long serialVersionUID = 6338890179869537364L;

    private String beginDate;
    private String endDate;
}
