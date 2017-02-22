package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 猪群变动事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorChangeGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -7231563115604588914L;

    /**
     * 猪群变动类型id
     */
    @NotNull(message = "changeType.not.null")
    private Long changeTypeId;

    /**
     * 猪群变动类型name
     */
    @NotNull(message = "changeType.not.null")
    private String changeTypeName;

    /**
     * 变动原因id
     */
    private Long changeReasonId;

    /**
     * 变动原因
     */
    private String changeReasonName;

    /**
     * 猪只数 公 + 母的和
     */
    @NotNull(message = "quantity.not.null")
    @Min(value = 1L, message = "quantity.not.lt.1")
    private Integer quantity;

    @NotNull(message = "boarQty.not.null")
    private Integer boarQty;

    @NotNull(message = "sowQty.not.null")
    private Integer sowQty;

    /**
     * 总活体重(单位:kg)
     */
    @NotNull(message = "weight.not.null")
    private Double weight;

    /**
     * 品种id
     */
    private Integer breedId;

    private String breedName;

    /**
     * 单价(分)(基础价)
     */
    @Min(value = 0L, message = "price.gt.0")
    private Long price;

    /**
     * 金额(分)
     */
    @Min(value = 0L, message = "amount.gt.0")
    private Long amount;

    /**
     * 客户id
     */
    private Long customerId;

    private String customerName;

    //////////////////////////////////////

    /**
     * 基础重量
     * @see io.terminus.doctor.event.enums.SaleBaseWeight
     */
    private Integer baseWeight;

    /**
     * 超出价格(分/kg)
     */
   // @Min(value = 0L, message = "price.gt.0")
    private Long overPrice;


    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(changeTypeName != null){
            map.put("变动类型", changeTypeName);
        }
        if(changeReasonName != null){
            map.put("变动原因", changeReasonName);
        }
        if(quantity != null){
            map.put("猪只数", quantity.toString());
        }
        if(weight != null){
            map.put("重量(kg)", weight.toString());
        }
        if(breedName != null){
            map.put("品种", breedName);
        }
        if(price != null){
            map.put("单价(元/kg)", Double.valueOf(price / 100.0).toString());
        }
        if(amount != null){
            map.put("金额(元)", Double.valueOf(amount / 100.0).toString());
        }
        if(customerName != null){
            map.put("客户", customerName);
        }
        if(baseWeight != null){
            map.put("基础重量", baseWeight.toString() + "KG");
        }
        if(overPrice != null){
            map.put("超出价格(元/kg)", Double.valueOf(overPrice / 100.0).toString());
        }
        return map;
    }
}
