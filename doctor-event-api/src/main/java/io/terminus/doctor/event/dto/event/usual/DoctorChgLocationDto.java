package io.terminus.doctor.event.dto.event.usual;

import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
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
 * Descirbe:
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorChgLocationDto extends AbstractPigEventInputDto implements Serializable{

    private static final long serialVersionUID = 8270765125209815779L;

    private Date changeLocationDate; //转舍日期

    private Long chgLocationFromBarnId; //源舍

    private String chgLocationFromBarnName; // 原舍名称

    private Long chgLocationToBarnId;   // 转舍Id

    private String chgLocationToBarnName;   // 转舍名称

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(chgLocationFromBarnName != null){
            map.put("原猪舍", chgLocationFromBarnName);
        }
        if(chgLocationToBarnName != null){
            map.put("转入猪舍", chgLocationToBarnName);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.changeLocationDate;
    }
}
