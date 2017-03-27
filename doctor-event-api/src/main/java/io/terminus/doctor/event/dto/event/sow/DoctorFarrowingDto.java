package io.terminus.doctor.event.dto.event.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪分娩事件Dto
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFarrowingDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 7823378636552520021L;

    @NotNull(message = "event.at.not.null")
    private Date farrowingDate; // 分娩日期

    //@NotEmpty(message = "nest.code.not.empty")
    private String nestCode; // 窝号

    private String bedCode; // 床号

    /**
     * @see io.terminus.doctor.event.enums.FarrowingType
     */
    @NotNull(message = "farrowing.type.not.null")
    private Integer farrowingType;  // 分娩类型

    @NotEmpty(message = "group.code.not.empty")
    private String groupCode;   // 仔猪猪群Code

    @Min(value = 0, message = "birth.nest.avg.not.less.zero")
    @NotNull(message = "birth.nest.avg.not.null")
    private double birthNestAvg;    //出生窝重

    private Integer dayAgeAvg;  // 平均日龄计算

    @Min(value = 0, message = "farrowing.live.count.less.zero")
    @NotNull(message = "farrowing.live.count.not.null")
    private Integer farrowingLiveCount; // 分娩猪的数量 = 活公猪 + 活母猪 = 健崽数量 + 弱仔数量 (拼窝后， 可能不相等， 差值 拼窝数量)

    private Integer liveSowCount;   //  活母猪数量

    private Integer liveBoarCount;  // 活公猪数量

    @Min(value = 0, message = "health.count.not.less.zero")
    @NotNull(message = "health.count.not.null")
    private Integer healthCount;    //健仔数量(断奶事件校验对应的母猪状态信息)

    @Min(value = 0, message = "weak.count.not.less.zero")
    @NotNull(message = "weak.count.not.null")
    private Integer weakCount;  //  弱崽数量

    private Integer mnyCount;   // 木乃伊数量

    private Integer jxCount;    // 畸形数量

    private Integer deadCount;  // 死亡数量

    private Integer blackCount; //  黑太数量

    private Long toBarnId;  // 猪群相关 待定！！

    private String toBarnName;  //猪群相关 待定！！

    private Integer farrowIsSingleManager;    //是否个体管理

    private String farrowStaff1;  //接生员1

    private String farrowStaff2;  //接生员2

    private String farrowRemark;    //标志

    @Override
    public Map<String, String> descMap(){
        Map<String, String> map = new LinkedHashMap<>();
        if(birthNestAvg > 0){
            map.put("出生窝重", Double.toString(birthNestAvg));
        }
        Integer total = MoreObjects.firstNonNull(farrowingLiveCount, 0)
                + MoreObjects.firstNonNull(mnyCount, 0)
                + MoreObjects.firstNonNull(jxCount, 0)
                + MoreObjects.firstNonNull(deadCount, 0)
                + MoreObjects.firstNonNull(blackCount, 0);
        map.put("总产仔数", total.toString());
        if(healthCount != null){
            map.put("健仔数", healthCount.toString());
        }
        if(weakCount != null){
            map.put("弱崽数", weakCount.toString());
        }
        if(groupCode != null){
            map.put("转入猪群", groupCode);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.farrowingDate;
    }

    @Override
    public String changeRemark() {
        return this.farrowRemark;
    }
}
