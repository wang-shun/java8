package io.terminus.doctor.web.front.warehouse.dto;

import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/7.
 */
@Data
public class DoctorMaterialDetailDto extends DoctorBasicMaterial implements Serializable{
    private static final long serialVersionUID = 1632347432712392992L;

    private Double lotNumber;
}
