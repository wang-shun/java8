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
 * Descirbe: 部分断奶事件录入
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPartWeanDto implements Serializable{

    private static final long serialVersionUID = 252972605944533095L;

    private Long pigId;

    private Date partWeanDate; //断奶日期

    private Integer partWeanPigletsCount;

    private Double partWeanAvgWeight;

    private String partWeanRemark;
}
