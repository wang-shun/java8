package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
import io.terminus.doctor.event.enums.FarrowingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
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
public class DoctorFarrowingDto extends AbstractPigEventInputDto implements Serializable{

    private static final long serialVersionUID = 7823378636552520021L;

    private Date farrowingDate; // 分娩日期

    private String nestCode; // 窝号

    private Long barnId;   // 分娩猪舍

    private String barnName;  //分娩猪舍名称

    private String bedCode; // 床号

    /**
     * @see io.terminus.doctor.event.enums.FarrowingType
     */
    private Integer farrowingType;  // 分娩类型

    /**
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    private Integer isHelp;     //  是否帮助

    private String groupCode;   // 仔猪猪群Code

    private double birthNestAvg;    //出生窝重

    private Integer dayAgeAvg;  // 平均日龄计算

    private Integer farrowingLiveCount; // 分娩猪的数量 = 活公猪 + 活母猪 = 健崽数量 + 弱仔数量 (拼窝后， 可能不相等， 差值 拼窝数量)

    private Integer liveSowCount;   //  活母猪数量

    private Integer liveBoarCount;  // 活公猪数量

    private Integer healthCount;    //健仔数量(断奶事件校验对应的母猪状态信息)

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
        Map<String, String> map = new HashMap<>();
        if(nestCode != null){
            map.put("窝号", nestCode);
        }
        if(barnName != null){
            map.put("分娩猪舍", barnName);
        }
        if(bedCode != null){
            map.put("床号", bedCode);
        }
        if(farrowingType != null){
            FarrowingType farrowingType1 = FarrowingType.from(farrowingType);
            if(farrowingType1 != null){
                map.put("分娩类型", farrowingType1.getDesc());
            }
        }
        if(isHelp != null){
            map.put("是否帮助", isHelp == 1 ? "是" : "否");
        }
        if(groupCode != null){
            map.put("仔猪猪群", groupCode);
        }
        if(birthNestAvg > 0){
            map.put("出生窝重", Double.toString(birthNestAvg));
        }
        if(dayAgeAvg != null){
            map.put("平均日龄", dayAgeAvg.toString());
        }
        if(farrowingLiveCount != null){
            map.put("分娩总数", farrowingLiveCount.toString());
        }
        if(liveSowCount != null){
            map.put("活母猪数", liveSowCount.toString());
        }
        if(liveBoarCount != null){
            map.put("活公猪数", liveBoarCount.toString());
        }
        if(healthCount != null){
            map.put("健仔数", healthCount.toString());
        }
        if(weakCount != null){
            map.put("弱崽数", weakCount.toString());
        }
        if(mnyCount != null){
            map.put("木乃伊数", mnyCount.toString());
        }
        if(jxCount != null){
            map.put("畸形数", jxCount.toString());
        }
        if(deadCount != null){
            map.put("死亡数", deadCount.toString());
        }
        if(blackCount != null){
            map.put("黑胎数", blackCount.toString());
        }
        if(farrowStaff1 != null){
            map.put("接生员1", farrowStaff1);
        }
        if(farrowStaff2 != null){
            map.put("接生员2", farrowStaff2);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.farrowingDate;
    }
}
