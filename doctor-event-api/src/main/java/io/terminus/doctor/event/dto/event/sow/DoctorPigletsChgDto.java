package io.terminus.doctor.event.dto.event.sow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 仔猪变动事件 (拼窝事件发生后，产生对应的仔猪变动的事件)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigletsChgDto implements Serializable{

    private static final long serialVersionUID = 2032098840987088160L;

    private Date pigletsChangeDate; // 仔猪变动日期

    private Integer pigletsCount;   // 仔猪数量

    private Integer sowPigletsCount;    // 仔母猪数量

    private Integer boarPigletsCount;   // 崽公猪数量

    private Long pigletsChangeType;   // 仔猪变动类型

    private String pigletsChangeTypeName;   // 仔猪变动类型内容

    private Long pigletsChangeReason;   // 仔猪变动原因

    private String pigletsChangeReasonName;   // 仔猪变动原因内容

    private Double pigletsWeight;  // 变动重量 (非必填)

    private Long pigletsPrice;   // 变动价格(分) （非必填）

    private Long pigletsSum; //  总价(分)（非必填）

    private Long pigletsCustomerId;    //客户Id （非必填）

    private String pigletsMark;  //标识(非必填)
}
