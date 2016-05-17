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
 * Descirbe: 断奶事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWeanDto implements Serializable {

    private static final long serialVersionUID = -8375069125388951376L;

    private Long pigId;

    private Date weanDate;

    private Integer pigletsCount;

    private Double avgWeight;

    private Long barnId;

    private String barnName;

    private String remark;
}
