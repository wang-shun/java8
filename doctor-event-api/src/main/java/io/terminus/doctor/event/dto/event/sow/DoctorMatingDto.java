package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
import io.terminus.doctor.event.enums.MatingType;
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
 * Descirbe: 母猪配种信息
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMatingDto extends AbstractPigEventInputDto implements Serializable {

    private static final long serialVersionUID = 2732269011148894160L;

    private Date matingDate; // 配种日期

    private Long matingBoarPigId;   //配种公猪Id

    private String matingBoarPigCode; //配种公猪号

    private Date judgePregDate; //预产日期

    /**
     * @see io.terminus.doctor.event.enums.MatingType
     */
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
