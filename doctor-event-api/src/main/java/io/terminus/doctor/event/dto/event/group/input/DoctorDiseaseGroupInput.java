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
 * Desc: 疾病事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorDiseaseGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -7993155207725161551L;

    /**
     * 疾病猪只数
     */
    @NotNull(message = "quantity.not.empty")
    @Min(value = 1L, message = "quantity.not.lt.1")
    private Integer quantity;

    /**
     * 疾病id
     */
    @NotNull(message = "diseaseId.not.null")
    private Long diseaseId;

    /**
     * 疾病名称
     */
    @NotEmpty(message = "diseaseName.not.empty")
    private String diseaseName;

    /**
     * 诊断人员id
     */
    private Long doctorId;

    /**
     * 诊断人员name
     */
    private String doctorName;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(quantity != null){
            map.put("猪只数", quantity.toString());
        }
        if(diseaseName != null){
            map.put("疾病", diseaseName);
        }
        if(doctorName != null){
           map.put("诊断人", doctorName);
        }
        return map;
    }
}
