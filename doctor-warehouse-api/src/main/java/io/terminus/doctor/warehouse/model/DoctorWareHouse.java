package io.terminus.doctor.warehouse.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DoctorWareHouse implements Serializable{

    private static final long serialVersionUID = -6738128699460454654L;

    private Long id;

    private String wareHouseName;

    private Long farmId;

    private String farmName;

    private Long managerId;

    private String managerName;

    private String address;

    private Integer wareHouseType;

    private Long materialTypeId;

    private String materialTypeName;

    private Integer isDefault;

    private Long creatorId;

    private String creatorName;

    private Long updatorId;

    private String updatorName;

    private Date createdAt;

    private Date updatedAt;
}
