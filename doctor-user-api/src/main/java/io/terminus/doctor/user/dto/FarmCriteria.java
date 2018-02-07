package io.terminus.doctor.user.dto;

import io.terminus.doctor.user.model.DoctorFarm;
import lombok.Data;

/**
 * Created by xjn on 18/1/8.
 * email:xiaojiannan@terminus.io
 */
@Data
public class FarmCriteria extends DoctorFarm{
    private static final long serialVersionUID = -2970210857976615145L;

    private String fuzzyName;
}
