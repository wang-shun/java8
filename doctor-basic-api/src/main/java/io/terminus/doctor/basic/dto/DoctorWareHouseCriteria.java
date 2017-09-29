package io.terminus.doctor.basic.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
@Data
public class DoctorWareHouseCriteria extends Pageable implements Serializable{


    private static final long serialVersionUID = -159548707105889710L;

    @NotNull(message = "farm.id.null")
    private Long farmId;

    @NotNull(message = "warehouse.type.null")
    private Integer type;

}
