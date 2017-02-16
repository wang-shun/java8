package io.terminus.doctor.web.core.dto;

import io.terminus.doctor.user.model.DoctorStaff;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/18.
 */

@Data
public class FarmStaff extends DoctorStaff implements Serializable{
    private static final long serialVersionUID = -691125151747762849L;
    private String realName;
}
