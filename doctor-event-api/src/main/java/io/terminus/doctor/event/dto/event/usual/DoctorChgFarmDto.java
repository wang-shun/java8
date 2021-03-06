package io.terminus.doctor.event.dto.event.usual;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 转场事件
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorChgFarmDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = -6702066337454157425L;

    @NotNull(message = "event.at.not.null")
    private Date chgFarmDate;  // 转场日期

    @NotNull(message = "from.farm.id.not.null")
    private Long fromFarmId;   // 原场Id

    private String fromFarmName;    // 原场名称

    @NotNull(message = "from.barn.id.not.null")
    private Long fromBarnId;    // 原设Id

    private String fromBarnName; // 原舍名称

    @NotNull(message = "to.farm.id.not.null")
    private Long toFarmId;  // 转场Id

    private String toFarmName;  // 转场名称

    @NotNull(message = "to.barn.id.not.null")
    private Long toBarnId;  //  转舍Id

    private String toBarnName;  // 转舍名称

    private Integer pigletsCount; // 仔猪数量

    private String remark;  // 注解

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(fromFarmName != null){
            map.put("原场", fromFarmName);
        }
        if(fromBarnName != null){
            map.put("原舍", fromBarnName);
        }
        if(toFarmName != null){
            map.put("转入猪场", toFarmName);
        }
        if(toBarnName != null){
            map.put("转入猪舍", toBarnName);
        }
        if(pigletsCount != null){
            map.put("仔猪数量", pigletsCount.toString());
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.chgFarmDate;
    }

    @Override
    public String changeRemark() {
        return this.remark;
    }
}
