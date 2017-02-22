package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.enums.MatingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪配种信息
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMatingDto extends BasePigEventInputDto implements Serializable {

    private static final long serialVersionUID = 2732269011148894160L;

    @NotNull(message = "event.at.not.null")
    private Date matingDate; // 配种日期

    @NotNull(message = "mating.boar.id.not.null")
    private Long matingBoarPigId;   //配种公猪Id

    @NotEmpty(message = "mating.boar.code.not.empty")
    private String matingBoarPigCode; //配种公猪号

    @NotNull(message = "judge.preg.date.not.null")
    private Date judgePregDate; //预产日期

    /**
     * @see io.terminus.doctor.event.enums.MatingType
     */
    @NotNull(message = "mating.type.not.null")
    private Integer matingType; // 配种类型

    private String mattingMark; // 配种mark

    @Override
    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        if(matingBoarPigCode != null){
            map.put("公猪号", matingBoarPigCode);
        }
        if(judgePregDate != null){
            map.put("预产日期", DateUtil.toDateString(judgePregDate));
        }
        if(matingType != null){
            MatingType e = MatingType.from(matingType);
            if(e != null){
                map.put("配种类型", e.getDesc());
            }
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.matingDate;
    }
}
