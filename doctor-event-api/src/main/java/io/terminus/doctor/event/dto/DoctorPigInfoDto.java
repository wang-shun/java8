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
 * Descirbe: 猪 分页列表数据信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigInfoDto implements Serializable{

    private static final long serialVersionUID = -3338994823651970751L;

    private Long id;

    private Integer pigType;

    private String pigCode;

    private Long status;

    private String statusName;

    private Integer dateAge;    // 日龄信息

    private Integer parity;

    private Date birthDay;

    private Date inFarmDate;

    private Long barnId;

    private String barnName;
}
