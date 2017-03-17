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
    private Date matingDate; // 配种日期
    private String matingBoarPigCode; //配种公猪号
    private Integer matingType; // 配种类型
    private String matingTypeName;
    private Date judgePregDate; //预产日期
    private String mattingMark; // 配种mark
    private String operatorName;
    private String creatorName;
    private Integer pigStatusAfter;
    private String pigStatusAfterName;

}
