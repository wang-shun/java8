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
 * Descirbe: 母猪体况信息
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorConditionDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 2731040792952612479L;

    @NotNull(message = "event.at.not.null")
    private Date conditionDate; //体况日期

    private Double conditionJudgeScore;    //体况评分

    private Double conditionWeight; // 体况重量

    @NotNull(message = "condition.back.weight.not.null")
    private Double conditionBackWeight; // 背膘

    private String conditionRemark; //体况注解

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(conditionJudgeScore != null){
            map.put("体况评分", conditionJudgeScore.toString());
        }
        if(conditionWeight != null){
            map.put("重量", conditionWeight.toString());
        }
        if(conditionBackWeight != null){
            map.put("背膘", conditionBackWeight.toString());
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.conditionDate;
    }

    @Override
    public String changeRemark() {
        return this.conditionRemark;
    }
}
