package io.terminus.doctor.event.dto.event.usual;

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
 * Descirbe: 转场事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorChgFarmDto implements Serializable{

    private static final long serialVersionUID = -6702066337454157425L;

    private Date chgFarmDate;  // 转场日期

    private Long fromFarmId;   // 原场Id

    private String fromFarmName;    // 原场名称

    private Long fromBarnId;    // 原设Id

    private String fromBarnName; // 原舍名称

    private Long toFarmId;  // 转场Id

    private String toFarmName;  // 转场名称

    private Long toBarnId;  //  转舍Id

    private String toBarnName;  // 转舍名称

    private Integer pigletsCount; // 仔猪数量

    private String remark;  // 注解
}
