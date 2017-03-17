package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorPregChkResultExportDto implements Serializable{

    private static final long serialVersionUID = -2475531639394695138L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date checkDate; //妊娠检查日期
    private Integer checkResult;    // 妊娠检查结果
    private String checkResultName;
    private String checkMark;
    private Integer npd;
    private String operatorName;
    private String creatorName;

}
