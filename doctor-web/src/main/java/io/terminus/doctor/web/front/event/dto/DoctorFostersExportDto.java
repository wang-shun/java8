package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorFostersExportDto implements Serializable{

    private static final long serialVersionUID = -3758975906258023892L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date fostersDate;   // 拼窝日期
    private String fosterReasonName;  //寄养原因名称
    private String fosterRemark;    // 拼窝标识
    private Integer fostersCount;   //  拼窝数量
    private String fosterSowCode; //代哺母猪
    private String operatorName;
}
