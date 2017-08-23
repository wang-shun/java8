package io.terminus.doctor.basic.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
@Data
public class DoctorWarehouseMaterialHandlerCriteria extends  Pageable implements Serializable{


    private static final long serialVersionUID = -4733441071864181476L;


    private Long managerID;


}
