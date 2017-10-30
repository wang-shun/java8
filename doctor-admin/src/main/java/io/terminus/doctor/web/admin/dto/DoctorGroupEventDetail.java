package io.terminus.doctor.web.admin.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by sunbo@terminus.io on 2017/9/7.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorGroupEventDetail extends DoctorGroupEvent implements Serializable {


    private static final long serialVersionUID = -2424143783277077146L;

    private Boolean isRollback;

}
