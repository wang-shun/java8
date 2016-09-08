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
 * Descirbe: 离场事件信息
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRemovalDto extends AbstractPigEventInputDto implements Serializable{

    private static final long serialVersionUID = -5166658905616894350L;

    private Date removalDate; // 离场时间

    private Long chgTypeId; // 变动类型Id basic 数据信息

    private String chgTypeName;   // 变动名称

    private Long chgReasonId;   // 变动原因 Id

    private String chgReasonName; // 变动原因

    private Long toBarnId;  // 转入事件信息

    private Double weight;  // 重量

    private Long price;  // 价格

    private Long sum; // 总量

    private Long customerId;    // 客户Id

    private String remark;  //注解

    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(chgTypeName != null){
            map.put("变动类型", chgTypeName);
        }
        if(chgReasonName != null){
            map.put("变动原因", chgReasonName);
        }
        if(weight != null){
            map.put("重量", weight.toString());
        }
        if(price != null){
            map.put("价格", price.toString());
        }
        if(sum != null){
            map.put("总量", sum.toString());
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.removalDate;
    }
}
