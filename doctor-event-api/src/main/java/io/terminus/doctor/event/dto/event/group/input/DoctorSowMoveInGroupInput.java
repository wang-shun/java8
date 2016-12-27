package io.terminus.doctor.event.dto.event.group.input;

import io.terminus.doctor.common.enums.PigType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Desc: 母猪事件触发的转入猪群事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorSowMoveInGroupInput extends DoctorMoveInGroupInput implements Serializable {
    private static final long serialVersionUID = -8922843195140861816L;

    /**
     * 公司id
     */
    @NotNull(message = "orgId.not.null")
    private Long orgId;

    /**
     * 公司名称
     */
    @NotEmpty(message = "orgName.not.empty")
    private String orgName;

    /**
     * 猪场id
     */
    @NotNull(message = "farmId.not.null")
    private Long farmId;

    /**
     * 猪场名称
     */
    @NotEmpty(message = "farmName.not.empty")
    private String farmName;

    /**
     * 猪群号
     */
    @NotEmpty(message = "groupCode.not.empty")
    private String groupCode;
    /**
     * 仔猪转入猪舍id
     */
    @NotNull(message = "barnId.not.null")
    private Long toBarnId;

    /**
     * 仔猪转入猪舍名称
     */
    @NotEmpty(message = "barnName.not.empty")
    private String toBarnName;

    /**
     * 猪类 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    @NotNull(message = "pig.type.not.null")
    private Integer pigType;

    /**
     * 品系id
     */
    private Long geneticId;

    /**
     * 品系name
     */
    private String geneticName;

    /**
     * 健仔数
     */
    private Integer healthyQty;

    /**
     * 弱仔数
     */
    private Integer weakQty;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = super.descMap();
        if(orgName != null){
            map.put("公司", orgName);
        }
        if(farmName != null){
            map.put("猪场", farmName);
        }
        if(groupCode != null){
            map.put("猪群号", groupCode);
        }
        if(toBarnName != null){
            map.put("转入猪舍", toBarnName);
        }
        if(pigType != null){
            PigType pigType1 = PigType.from(pigType);
            if(pigType1 != null){
                map.put("猪类型", pigType1.getDesc());
            }
        }
        if(geneticName != null){
            map.put("品系", geneticName);
        }
        return map;
    }
}
