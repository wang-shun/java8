package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorChgFarmExportDto implements Serializable {
    private static final long serialVersionUID = 1878561505586914646L;
    private String pigCode;
    private Date chgFarmDate;  // 转场日期
    private String fromFarmName;    // 原场名称
    private String fromBarnName; // 原舍名称
    private String toFarmName;  // 转场名称
    private String toBarnName;  // 转舍名称
    private Integer pigletsCount; // 仔猪数量
    private String remark;  // 注解
    private String operatorName;
}
