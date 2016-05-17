package io.terminus.doctor.warehouse.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DoctorMaterialInfo implements Serializable{

    private static final long serialVersionUID = -1044720613700425597L;

    private Long id;

    private Long farmId;

    private String farmName;

    private String remark;

    private Long unitGroupId;

    private String unitGroupName;

    private Long unitId;

    private String unitName;

    private Long price;

    private String extra;

    private Long creatorId;

    private String creatorName;

    private Long updatorId;

    private String updatorName;

    private Date createdAt;

    private Date updatedAt;

}