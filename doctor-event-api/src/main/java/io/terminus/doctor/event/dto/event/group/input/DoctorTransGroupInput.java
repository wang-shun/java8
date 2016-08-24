package io.terminus.doctor.event.dto.event.group.input;

import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 猪群转群事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTransGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -8219074426917150673L;

    /**
     * 转入猪舍id
     */
    @NotNull(message = "to.barn.not.null")
    private Long toBarnId;

    @NotEmpty(message = "to.barn.not.null")
    private String toBarnName;

    /**
     * 转入猪群id
     */
    private Long toGroupId;

    /**
     * 转入猪群号
     */
    @NotNull(message = "to.group.not.null")
    private String toGroupCode;

    /**
     * 是否新建猪群 0:否 1:是
     * @see IsOrNot
     */
    @NotNull(message = "is.create.group.not.null")
    private Integer isCreateGroup;

    /**
     * 品种id
     */
    private Long breedId;

    private String breedName;

    /**
     * 猪只数 公 + 母的和
     */
    @NotNull(message = "quantity.not.null")
    private Integer quantity;

    @NotNull(message = "boarQty.not.null")
    private Integer boarQty;

    @NotNull(message = "boarQty.not.null")
    private Integer sowQty;

    /**
     * 均重(kg)
     * 转群以此字段为主, 转场已总重为主
     */
    @NotNull(message = "weight.not.null")
    private Double avgWeight;

    /**
     * 总活体重(kg)
     */
    @NotNull(message = "weight.not.null")
    private Double weight;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    private Integer source;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(toBarnName != null){
            map.put("转入猪舍", toBarnName);
        }
        if(toGroupCode != null){
            map.put("转入猪群", toGroupCode);
        }
        if(isCreateGroup != null){
            map.put("是否新建猪群", isCreateGroup == 1 ? "是" : "否");
        }
        if(breedName != null){
            map.put("品种", breedName);
        }
        if(boarQty != null){
            map.put("公猪数", boarQty.toString());
        }
        if(sowQty != null){
            map.put("母猪数", sowQty.toString());
        }
        if(avgWeight != null){
            map.put("均重(Kg)", avgWeight.toString());
        }
        if(weight != null){
            map.put("总活体重(Kg)", weight.toString());
        }
        if(source != null){
            map.put("来源", PigSource.from(source).getDesc());
        }
        return map;
    }
}
