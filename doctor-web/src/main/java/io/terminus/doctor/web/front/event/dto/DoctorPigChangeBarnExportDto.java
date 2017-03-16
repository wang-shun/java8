package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorPigChangeBarnExportDto implements Serializable{
    private static final long serialVersionUID = 4436809727648640270L;

    private String pigCode;
    private Integer parity;
    private Date changeLocationDate;
    private String chgLocationFromBarnName;
    private String chgLocationToBarnName;
    private Integer pigStatusAfter;
    private String remark;
    private String creatorName;

}
