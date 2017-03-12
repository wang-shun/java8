package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Objects;

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

    private Integer toBarnType;

    private Double weight;


    public boolean equals(DoctorTurnSeedGroupEvent event){
        if(!Objects.equals(this.pigCode, event.pigCode) ||
                !Objects.equals(this.motherEarCode, event.motherEarCode) ||
                !Objects.equals(this.earCode, event.earCode) ||
                !Objects.equals(this.transInAt, event.transInAt) ||
                !Objects.equals(this.birthDate, event.birthDate) ||
                !Objects.equals(this.breedId, event.breedId) ||
                !Objects.equals(this.breedName, event.breedName) ||
                !Objects.equals(this.geneticId, event.geneticId) ||
                !Objects.equals(this.geneticName, event.geneticName) ||
                !Objects.equals(this.toBarnId, event.toBarnId) ||
                !Objects.equals(this.toBarnName, event.toBarnName) ||
                !Objects.equals(this.toBarnType, event.toBarnType)){
            return false;
        }
        return true;
    }
}
