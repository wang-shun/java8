package io.terminus.doctor.event.dto.event.usual;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 体况信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorConditionDto implements Serializable{

    private static final long serialVersionUID = 2731040792952612479L;

    private Long pigId;

    private Integer judgeScore;

    private Double weight;

    private Double backWeight; // 背膘

    private String remark;
}
