package io.terminus.doctor.event.dto.event.sow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 仔猪拼窝事件(自动生成代哺的事件)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFostersDto implements Serializable{

    private static final long serialVersionUID = 2998287596879859648L;

    private String fostersDate;   // 拼窝日期

    private Integer fostersCount;   //  拼窝数量

    private Integer sowFostersCount;    // 拼窝母猪数量

    private Integer boarFostersCount;   // 拼窝公猪数量

    private Double fosterTotalWeight;   //拼窝总重量

    private Long fosterReason;  //寄养原因id

    private String fosterReasonName;  //寄养原因名称

    private Long fosterSowId;   // 拼窝母猪Id

    private String fosterSowCode; // 拼窝母猪code

    private String fosterSowOutId; // 拼窝母猪outId(做关联用)

    private String fosterRemark;    // 拼窝标识

    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        if(fostersCount != null){
            map.put("拼窝数量", fostersCount.toString());
        }
        if(sowFostersCount != null){
            map.put("拼窝母猪数量", sowFostersCount.toString());
        }
        if(boarFostersCount != null){
            map.put("拼窝公猪数量", boarFostersCount.toString());
        }
        if(fosterTotalWeight != null){
            map.put("拼窝总重量", fosterTotalWeight.toString());
        }
        if(fosterReasonName != null){
            map.put("寄养原因", fosterReasonName);
        }
        if(fosterSowCode != null){
            map.put("被拼窝母猪", fosterSowCode);
        }
        return map;
    }
}
