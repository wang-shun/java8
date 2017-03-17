package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorWeanExportDto implements Serializable{

    private static final long serialVersionUID = -8287088242622695944L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date partWeanDate; //断奶日期
    private Double partWeanAvgWeight;   //断奶平均重量
    private String partWeanRemark;  //部分断奶标识
    private Integer qualifiedCount; // 合格数量
    private Integer notQualifiedCount; //不合格的数量
    private Long chgLocationToBarnId;   // 转舍Id
    private Integer weanPigletsCount; //已断奶数
    private String operatorName;
    private String creatorName;

}
