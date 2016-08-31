package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 商品猪转为种猪事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTurnSeedGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -2955174319148999586L;

    /**
     * 转种猪后的猪号
     */
    @NotNull(message = "pig.code.not.null")
    private String pigCode;

    /**
     * 母亲猪 耳缺号
     */
    private String motherEarCode;

    /**
     * 耳缺号
     */
    private String earCode;

    /**
     * 出生日期
     */
    @NotNull(message = "birthdate.not.null")
    private String birthDate;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    /**
     * 转入猪舍
     */
    @NotNull(message = "barnId.not.null")
    private Long toBarnId;

    @NotEmpty(message = "barnId.not.null")
    private String toBarnName;

    /**
     * 重量(kg)
     */
    @Min(value = 0, message = "weight.gt.0")
    private Double weight;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> descMap = new HashMap<>();
        if(pigCode != null){
            descMap.put("种猪号", this.pigCode);
        }
        if(birthDate != null){
            descMap.put("出生日期", this.birthDate);
        }
        if(toBarnName != null){
            descMap.put("转入猪舍", this.toBarnName);
        }
        if(this.earCode != null){
            descMap.put("耳缺号", this.earCode);
        }
        if(this.motherEarCode != null){
            descMap.put("母亲耳缺号", this.motherEarCode);
        }
        if(this.weight != null){
            descMap.put("重量", this.weight.toString());
        }
        if(this.breedName != null){
            descMap.put("品种", this.breedName);
        }
        if(this.geneticName != null){
            descMap.put("品系", this.geneticName);
        }
        return descMap;
    }
}
