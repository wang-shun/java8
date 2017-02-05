package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/5.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPigEventDetail implements Serializable {
    private static final long serialVersionUID = 6810984617242339516L;

    private DoctorPigEvent doctorPigEvent;
    private Boolean isRollback;
}
