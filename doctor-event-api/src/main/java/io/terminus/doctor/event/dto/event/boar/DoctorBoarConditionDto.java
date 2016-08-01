package io.terminus.doctor.event.dto.event.boar;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 公猪体况事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/1
 */
@Data
public class DoctorBoarConditionDto implements Serializable {
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
     * 重量
     */
    private Double weight;
}
