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
     * 转种猪后的id
     */
    private Long pigId;

    /**
     * 转种猪后的猪号
     */
    private String pigCode;

    /**
     * 母亲猪号
     */
    private String motherPigCode;

    /**
     * 转入日期
     */
    private String transInAt;

    /**
     * 出生日期
     */
    private String birthDate;

    /**
     * 性别 0:种母猪 1:种公猪(ESex)
     * @see io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent.Sex
     */
    private Integer sex;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    private Long toBarnId;

    private String toBarnName;

    public enum Sex {
        SOW(0, "种母猪"),
        BOAR(1, "种公猪");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        Sex(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Sex from(String desc) {
            for (Sex sex : Sex.values()) {
                if (Objects.equal(sex.desc, desc)) {
                    return sex;
                }
            }
            return null;
        }

        public static Sex from(int value) {
            for (Sex sex : Sex.values()) {
                if (Objects.equal(sex.value, value)) {
                    return sex;
                }
            }
            return null;
        }
    }
}
