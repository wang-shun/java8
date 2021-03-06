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
 * Descirbe: 仔猪变动事件 (拼窝事件发生后，产生对应的仔猪变动的事件)
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigletsChgDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 2032098840987088160L;

    @NotNull(message = "event.at.not.null")
    private Date pigletsChangeDate; // 仔猪变动日期

    @Min(value = 0, message = "piglets.count.not.less.zero")
    @NotNull(message = "piglets.count.not.null")
    private Integer pigletsCount;   // 仔猪数量

    private Integer sowPigletsCount;    // 仔母猪数量

    private Integer boarPigletsCount;   // 崽公猪数量

    @NotNull(message = "piglets.change.type.not.null")
    private Long pigletsChangeType;   // 仔猪变动类型

    private String pigletsChangeTypeName;   // 仔猪变动类型内容

    private Long pigletsChangeReason;   // 仔猪变动原因

    private String pigletsChangeReasonName;   // 仔猪变动原因内容

    @Min(value = 0, message = "piglets.weight.not.less.zero")
    @NotNull(message = "piglets.weight.not.null")
    private Double pigletsWeight;  // 变动重量 (必填)

    private Long pigletsPrice;   // 变动价格(分) （非必填）

    private Long pigletsSum; //  总价(分)（非必填）

    private Long pigletsCustomerId;    //客户Id （非必填）

    private String pigletsCustomerName;    //客户姓名 （非必填）

    private String pigletsMark;  //标识(非必填)

//    private String pigCode;
//
//    private Long barnId;
//
//    private String barnName;

    @Override
    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        if(pigletsCount != null){
            map.put("仔猪数量", pigletsCount.toString());
        }
        if(sowPigletsCount != null){
            map.put("仔母猪数量", sowPigletsCount.toString());
        }
        if(boarPigletsCount != null){
            map.put("仔公猪数量", boarPigletsCount.toString());
        }
        if(pigletsChangeTypeName != null){
            map.put("变动类型", pigletsChangeTypeName);
        }
        if(pigletsChangeReasonName != null){
            map.put("变动原因", pigletsChangeReasonName);
        }
        if(pigletsWeight != null){
            map.put("变动重量", pigletsWeight.toString());
        }
        if(pigletsPrice != null){
            map.put("变动价格", Double.valueOf(pigletsPrice / 100.0).toString());
        }
        if(pigletsSum != null){
            map.put("总价", Double.valueOf(pigletsSum / 100.0).toString());
        }
        if(pigletsCustomerName != null){
            map.put("客户", pigletsCustomerName);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.pigletsChangeDate;
    }

    @Override
    public String changeRemark() {
        return this.pigletsMark;
    }
}
