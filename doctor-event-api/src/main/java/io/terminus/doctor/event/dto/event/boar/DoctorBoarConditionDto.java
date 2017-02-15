package io.terminus.doctor.event.dto.event.boar;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 公猪体况事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBoarConditionDto extends BasePigEventInputDto implements Serializable{
    private static final long serialVersionUID = -8382360464209200834L;

    /**
     * 检查日期
     */
    @NotNull(message = "event.at.not.null")
    private Date checkAt;

    /**
     * 活力得分
     */
    private Integer scoreHuoli;

    /**
     * 密度得分
     */
    private Integer scoreMidu;

    /**
     * 形态得分
     */
    private Integer scoreXingtai;

    /**
     * 数量得分
     */
    private Integer scoreShuliang;

    /**
     * 重量(必填)
     */
    @Min(value = 0, message = "weight.not.less.zero")
    @NotNull(message = "weight.not.null")
    private Double weight;

    /**
     * 备注
     */
    private String remark;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(scoreHuoli != null){
            map.put("活力得分", scoreHuoli.toString());
        }
        if(scoreMidu != null){
            map.put("密度得分", scoreMidu.toString());
        }
        if(scoreXingtai != null){
            map.put("形态得分", scoreXingtai.toString());
        }
        if(scoreShuliang != null){
            map.put("数量得分", scoreShuliang.toString());
        }
        if(weight != null){
            map.put("重量", weight.toString());
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.checkAt;
    }


//    public static void main(String[] args) {
//        DoctorBoarConditionDto dto = new DoctorBoarConditionDto();
//        dto.setCheckAt(new Date());
//        dto.setWeight(-1d);
//        Method method = null;
//        try {
//            method = dto.getClass().getMethod("setDto", DoctorBoarConditionDto.class);
//        } catch (Exception e) {
//
//        }
//        Object[] objs= {dto};
//        DoctorInvokeValidator.instance().validateParams(dto, method, objs);
//    }
//
//    public void setDto(@Valid DoctorBoarConditionDto dto) {
//
//    }
}
