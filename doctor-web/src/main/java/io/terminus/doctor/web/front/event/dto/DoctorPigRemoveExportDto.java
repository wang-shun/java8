package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorPigRemoveExportDto implements Serializable{

    private static final long serialVersionUID = 1120399206584387265L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date removalDate;
    private String chgTypeName;
    private String chgReasonName;
    private Double weight;
    private Long price;
    private Long amount;
    private String customerName;
    private String operatorName;
    private String remark;
    private String creatorName;


}
