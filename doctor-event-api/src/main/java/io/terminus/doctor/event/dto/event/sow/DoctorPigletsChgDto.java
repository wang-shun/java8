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

    private Long pigId;

    private Date pigletsChangeDate;

    private Integer pigletsCount;   // 仔猪数量

    private Integer sowPigletsCount;

    private Integer boarPigletsCount;

    private Long pigletsChangeReason;

    private Double weight;

    private Double price;

    private Double sum;

    private Long customerId;    //客户Id

    private String mark;
}
