package io.terminus.doctor.event.dto.event.group;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Desc: 商品猪转为种猪事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTurnSeedGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = -1375341874551616284L;

    /**
     * 转种猪后的猪号
     */
    private String pigCode;

    /**
     * 母亲猪 耳缺号
     */
    private String motherEarCode;

    /**
     * 耳缺号
     */
    private String earCode;

    /**
     * 转入日期
     */
    private String transInAt;

    /**
     * 出生日期
     */
    private String birthDate;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    private Long toBarnId;

    private String toBarnName;

}
