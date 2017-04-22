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

import static io.terminus.common.utils.Arguments.notEmpty;

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
     * @see io.terminus.doctor.event.enums.InType
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

    private Integer fromBarnType;

    /**
     * 来源猪群id
     */
    private Long fromGroupId;

    private String fromGroupCode;

    /**
     * 猪只数 公 + 母的和
     */
    @NotNull(message = "quantity.not.null")
    @Min(value = 1L, message = "quantity.not.lt.1")
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

    /**
     * 健仔数
     */
    private Integer healthyQty;

    /**
     * 弱仔数
     */
    private Integer weakQty;

    /**
     * 触发转入事件的母猪id
     */
    private Long sowId;

    /**
     * 触发转入事件的母猪号
     */
    private String sowCode;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(inTypeName != null){
            map.put("转移类型", inTypeName);
        }
        if(source != null){
            PigSource source1 = PigSource.from(source);
            if(source1 != null){
                map.put("来源", source1.getDesc());
            }
        }
        if(sex != null){
            DoctorGroupTrack.Sex sex1 = DoctorGroupTrack.Sex.from(sex);
            if(sex1 != null){
                map.put("性别", sex1.getDesc());
            }
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
        if(quantity != null){
            map.put("猪只数", quantity.toString());
        }
        if(avgDayAge != null){
            map.put("平均日龄", avgDayAge.toString());
        }
        if(avgWeight != null){
            map.put("平均体重(Kg)", avgWeight.toString());
        }
        if(amount != null){
            map.put("总价值", Double.valueOf(amount / 100.0).toString());
        }
        if (notEmpty(sowCode)) {
            map.put("母猪号", sowCode);
        }
        return map;
    }
}
