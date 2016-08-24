package io.terminus.doctor.event.dto.event.group.input;

import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 防疫事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorAntiepidemicGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -1677728961844574978L;

    /**
     * 防疫猪只数
     */
    @NotNull(message = "quantity.not.empty")
    @Min(value = 1L, message = "quantity.not.lt.1")
    private Integer quantity;

    /**
     *  疫苗id
     */
    @NotNull(message = "vaccin.id.not.null")
    private Long vaccinId;

    /**
     *  疫苗名称
     */
    @NotEmpty(message = "vaccin.name.not.empty")
    private String vaccinName;

    /**
     *  防疫结果: 1:阳性 -1:阴性
     *  @see io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent.VaccinResult
     */
    private Integer vaccinResult;

    /**
     * 防疫项目id
     */
    private Long vaccinItemId;

    /**
     * 防疫项目名称
     */
    private String vaccinItemName;

    /**
     *  防疫人员id
     */
    private Integer vaccinStaffId;

    /**
     *  防疫人员名称
     */
    private String vaccinStaffName;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> descMap = new HashMap<>();
        if(quantity != null){
            descMap.put("猪数量", this.quantity.toString());
        }
        if(vaccinName != null){
            descMap.put("疫苗", this.vaccinName);
        }
        if(DoctorAntiepidemicGroupEvent.VaccinResult.from(vaccinResult) != null){
            descMap.put("结果", DoctorAntiepidemicGroupEvent.VaccinResult.from(vaccinResult).getDesc());
        }
        if(vaccinItemName != null){
            descMap.put("防疫项目", vaccinItemName);
        }
        if(vaccinStaffName != null){
            descMap.put("防疫人员", vaccinStaffName);
        }
        return descMap;
    }
}
