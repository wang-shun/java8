package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorPigMatingExportDto implements Serializable{

    private static final long serialVersionUID = 6999342088142498424L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date matingDate;
    private String matingStaff;
    private String matingBoarPigCode;
    private Integer doctorMateType;
    private Date judgePregDate;
    private String mattingMark;
    private String operatorName;
    private Integer pigStatusAfter;

}
