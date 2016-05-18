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
 * Descirbe: 母猪妊娠检查事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPregChkResultDto implements Serializable{

    private static final long serialVersionUID = 2879901632920960216L;

    private Long pigId;

    private Date checkDate;

    private Integer checkResult;

    private String mark;
}
