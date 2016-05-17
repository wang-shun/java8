package io.terminus.doctor.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪详细信息列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSowPigInfoDetailDto implements Serializable{

    private static final long serialVersionUID = -4815878886196973012L;

    private Long pigId;

    private String tips;

    private String pigCode;

    private Double weight;

    private Integer dayAge;

    private Double npd;

    private Integer parity;

    private Date inFarmDate;

    private Double healthCountAvg;  // 窝均健崽数量

}
