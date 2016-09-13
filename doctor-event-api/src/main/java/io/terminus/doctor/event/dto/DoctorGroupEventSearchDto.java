package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xiao on 16/9/13.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorGroupEventSearchDto extends DoctorGroupEvent implements Serializable{

    private static final long serialVersionUID = -7273296714124053316L;

    private Date beginDate;
    private Date endDate;
}
