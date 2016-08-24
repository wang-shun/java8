package io.terminus.doctor.event.dto.event.group.input;

import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 转入猪群事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorMoveInGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -2457482464847828070L;

    /**
     * 猪群转移类型
     * @see io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent.InType
     */
    @NotNull(message = "inType.not.null")
    private Integer inType;

    /**
     * 猪群转移类型名
     */
    @NotEmpty(message = "inType.not.null")
    private String inTypeName;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    @NotNull(message = "source.not.null")
    private Integer source;

    /**
     * 性别 1:混合 2:母 3:公
     * @see io.terminus.doctor.event.model.DoctorGroupTrack.Sex
     */
    @NotNull(message = "sex.not.null")
    private Integer sex;

    /**
     * 品种id
     */
    private Long breedId;

    private String breedName;

    /**
     * 来源猪舍id
     */
    private Long fromBarnId;

    private String fromBarnName;

    /**
     * 来源猪群id
     */
    private Long fromGroupId;

    private String fromGroupCode;

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
     * 平均日龄
     */
    @NotNull(message = "avgDayAge.not.null")
    private Integer avgDayAge;

    /**
     * 平均体重(单位:kg)
     */
    @NotNull(message = "avgWeight.not.null")
    private Double avgWeight;

    /**
     * 总价值(分)
     */
    @Min(value = 0L, message = "amount.gt.0")
    private Long amount;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(inTypeName != null){
            map.put("转移类型", inTypeName);
        }
        if(source != null){
            map.put("来源", PigSource.from(source).getDesc());
        }
        if(sex != null){
            map.put("性别", DoctorGroupTrack.Sex.from(sex).getDesc());
        }
        if(breedName != null){
            map.put("品种", breedName);
        }
        if(fromBarnName != null){
            map.put("来源猪舍", fromBarnName);
        }
        if(fromGroupCode != null){
            map.put("来源猪群", fromGroupCode);
        }
        if(boarQty != null){
            map.put("公猪数", boarQty.toString());
        }
        if(sowQty != null){
            map.put("母猪数", sowQty.toString());
        }
        if(avgDayAge != null){
            map.put("平均日龄", avgDayAge.toString());
        }
        if(avgWeight != null){
            map.put("平均体重(Kg)", avgWeight.toString());
        }
        if(amount != null){
            map.put("总价值", Long.valueOf(amount / 100).toString());
        }
        return map;
    }
}
