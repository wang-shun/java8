package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DoctorPig implements Serializable{

    private static final long serialVersionUID = -5981942073814626473L;

    private Long id;

    private Long orgId;

    private String orgName;

    private Long farmId;

    private String farmName;

    private String outId;

    private String pigCode;

    private Integer pigType;

    private Long pigFatherId;

    private Long pigMotherId;

    private Integer source;

    private Date birthDate;

    private Double birthWeight;

    private Date inFarmDate;

    private Integer inFarmDayAge;

    private Long initBarnId;

    private String initBarnName;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    private String extra;

    private String remark;

    private Long creatorId;

    private String creatorName;

    private Long updatorId;

    private String updatorName;

    private Date createdAt;

    private Date updatedAt;

}