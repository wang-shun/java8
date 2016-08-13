package io.terminus.doctor.basic.dto;

import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBasicMaterialSearchDto extends DoctorBasicMaterial implements Serializable {
    private static final long serialVersionUID = 2499677871194430638L;

    private Integer pageNo;

    private Integer size;

    public DoctorBasicMaterialSearchDto(Integer pageNo, Integer size, String srm, String name, Integer type) {
        this.pageNo = pageNo;
        this.size = size;
        setSrm(srm);
        setName(name);
        setType(type);
    }
}
