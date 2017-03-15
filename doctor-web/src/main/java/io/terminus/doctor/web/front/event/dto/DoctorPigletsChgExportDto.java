package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorPigletsChgExportDto implements Serializable{

    private static final long serialVersionUID = 6559330090081849114L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date pigletsChangeDate; // 仔猪变动日期
    private Integer pigletsCount;   // 仔猪数量
    private Integer boarPigletsCount;   // 崽公猪数量
    private String pigletsChangeTypeName;   // 仔猪变动类型内容
    private String pigletsChangeReasonName;
    private Double pigletsWeight;  // 变动重量 (必填)
    private Long pigletsPrice;   // 变动价格(分) （非必填）
    private Long pigletsSum; //  总价(分)（非必填）
    private String pigletsCustomerName;    //客户姓名 （非必填）
    private String pigletsMark;  //标识(非必填)
    private String updatorName;
}

