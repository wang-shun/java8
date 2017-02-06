package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
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
public class DoctorGroupEventDetail extends DoctorGroupEvent implements Serializable {
    private static final long serialVersionUID = 3941077199183497746L;

    private Boolean isRollback;
}
