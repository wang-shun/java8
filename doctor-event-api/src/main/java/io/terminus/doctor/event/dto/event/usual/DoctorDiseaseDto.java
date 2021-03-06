package io.terminus.doctor.event.dto.event.usual;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 疾病事件
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDiseaseDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 7557956788545103933L;

    @NotNull(message = "event.at.not.null")
    private Date diseaseDate;   // 疾病日期

    @NotNull(message = "disease.id.not.null")
    private Long diseaseId; // 疾病Id

    private String diseaseName; // 疾病名称

    private String diseaseStaff;    // 疾病操作人员

    private String diseaseRemark;   // 疾病注解

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(diseaseName != null){
            map.put("疾病", diseaseName);
        }
        if(diseaseStaff != null){
            map.put("操作人", diseaseStaff);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.diseaseDate;
    }

    @Override
    public String changeRemark() {
        return this.diseaseRemark;
    }
}
