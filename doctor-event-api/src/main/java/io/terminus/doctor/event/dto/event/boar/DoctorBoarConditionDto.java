package io.terminus.doctor.event.dto.event.boar;

import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class DoctorBoarConditionDto extends AbstractPigEventInputDto implements Serializable{
    private static final long serialVersionUID = -8382360464209200834L;

    /**
     * 检查日期
     */
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
}
