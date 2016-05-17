package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DoctorPigSnapshot implements Serializable{

    private static final long serialVersionUID = -7819883927315891506L;

    private Long id;

    private Long orgId;

    private Long farmId;

    private Long pigId;

    private Long eventId;

    private String pigInfo;

    private Date createdAt;

    private Date updatedAt;

}