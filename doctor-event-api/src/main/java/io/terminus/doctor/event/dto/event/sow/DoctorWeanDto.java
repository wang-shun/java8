package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 部分断奶事件录入
 * (每次断奶都是部分断奶, 旧软件的最后一次断奶会触发转舍事件, 新软件不会触发)
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWeanDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 252972605944533095L;

    @NotNull(message = "event.at.not.null")
    private Date partWeanDate; //断奶日期

    @Min(value = 0, message = "part.wean.piglets.count.not.less.zero")
    @NotNull(message = "part.wean.piglets.count.not.null")
    private Integer partWeanPigletsCount; //部分断奶数量

    @Min(value = 0, message = "part.wean.avg.weight.not.less.zero")
    @NotNull(message = "part.wean.avg.weight.not.null")
    private Double partWeanAvgWeight;   //断奶平均重量

    private String partWeanRemark;  //部分断奶标识

    private Integer qualifiedCount; // 合格数量

    private Integer notQualifiedCount; //不合格的数量

    @Min(value = 0, message = "farrowing.live.count.less.zero")
    @NotNull(message = "farrowing.live.count.not.null")
    private Integer farrowingLiveCount; //分娩存活数

    private Long chgLocationToBarnId;   // 转舍Id

    @Min(value = 0, message = "wean.piglets.count.not.less.zero")
    @NotNull(message = "wean.piglets.count.not.null")
    private Integer weanPigletsCount; //已断奶数

    @Override
    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        if(partWeanPigletsCount != null){
            map.put("断奶数量", partWeanPigletsCount.toString());
        }
        if(partWeanAvgWeight != null){
            map.put("断奶平均重量", partWeanAvgWeight.toString());
        }
//        if(qualifiedCount != null){
//            map.put("合格数量", qualifiedCount.toString());
//        }
//        if(notQualifiedCount != null){
//            map.put("不合格数量", notQualifiedCount.toString());
//        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.partWeanDate;
    }

    @Override
    public String changeRemark() {
        return this.partWeanRemark;
    }
}
