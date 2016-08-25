package io.terminus.doctor.event.dto.event.usual;

import lombok.AllArgsConstructor;
import lombok.Data;
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
 * Descirbe: 体况信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorConditionDto implements Serializable{

    private static final long serialVersionUID = 2731040792952612479L;

    private Date conditionDate; //体况日期

    private Double conditionJudgeScore;    //体况评分

    private Double conditionWeight; // 体况重量

    private Double conditionBackWeight; // 背膘

    private String conditionRemark; //体况注解


    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
// TODO
        return map;
    }
}
