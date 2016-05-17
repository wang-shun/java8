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

    private Long pigId;

    private Date chgFarmDate;

    private Long fromFarmId;

    private String fromFarmName;

    private Long fromBarnId;

    private String fromBarnName;

    private Long toFarmId;

    private String toFarmName;

    private Long toBarnId;

    private String toBarnName;

    private Integer pigletsCount;

    private String remark;
}
