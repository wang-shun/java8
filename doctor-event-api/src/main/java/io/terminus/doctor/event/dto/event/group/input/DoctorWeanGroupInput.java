package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 12:16 17/3/11
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorWeanGroupInput extends BaseGroupInput implements Serializable {

    private static final long serialVersionUID = 626967961096215087L;

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

    private Long groupId; //关联的猪群

    @Override
    public Map<String, String> descMap() {
        Map<String, String> descMap = new HashMap<>();
        if(partWeanDate != null){
            descMap.put("断奶日期", this.partWeanDate.toString());
        }
        if(partWeanPigletsCount != null){
            descMap.put("断奶数量", this.partWeanPigletsCount.toString());
        }
        if(partWeanAvgWeight != null){
            descMap.put("断奶均重", this.partWeanAvgWeight.toString());
        }
        if(this.partWeanRemark != null){
            descMap.put("备注", this.partWeanRemark);
        }
        if(this.qualifiedCount != null){
            descMap.put("合格数", this.qualifiedCount.toString());
        }
        if(this.notQualifiedCount != null){
            descMap.put("不合格数", this.notQualifiedCount.toString());
        }
        return descMap;
    }
}
