package io.terminus.doctor.event.dto.event.usual;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 免疫信息结果
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorVaccinationDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 183960403500452272L;

    private Date vaccinationDate; // 免疫日期

    private Long vaccinationId; // 疫苗名称Id

    private String vaccinationName; // 疫苗名称

//    private Integer vaccinationResult;  // 防疫结果

    private Long vaccinationItemId; // 免疫项目Id

    private String vaccinationItemName; //免疫项目 名称

    private Long vaccinationStaffId;    // 防疫人员Id

    private String vaccinationStaffName;    //防疫人员名称

    private String vaccinationRemark;   // 防疫注解

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(vaccinationName != null){
            map.put("疫苗", vaccinationName);
        }
        if(vaccinationItemName != null){
            map.put("免疫项目", vaccinationItemName);
        }
        if(vaccinationStaffName != null){
            map.put("防疫人员", vaccinationStaffName);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.vaccinationDate;
    }
}
