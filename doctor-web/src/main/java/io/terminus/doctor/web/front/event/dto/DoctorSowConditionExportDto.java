package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/17.
 */
@Data
public class DoctorSowConditionExportDto implements Serializable {
    private static final long serialVersionUID = 734083595159904879L;

    private String pigCode;
    private String barnName;
    private Date conditionDate;
    private Double conditionJudgeScore;    //体况评分

    private Double conditionWeight; // 体况重量

    private Double conditionBackWeight; // 背膘

    private String conditionRemark; //体况注解

    private String operatorName;
    private String creatorName;
}
