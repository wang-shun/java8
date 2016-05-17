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
 * Descirbe: 窝重测量事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorLitterWeightDto implements Serializable {

    private static final long serialVersionUID = -3360271781401622417L;

    private Long pigId;

    private Date measureDate;

    private Integer pigletsCount;   // !! 不可变的数量， 校验

    private Double nestWeight;

    private String remark;
}
