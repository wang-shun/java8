package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Desc: 转入猪群事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorMoveInGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = 7901925323957688027L;

    /**
     * 猪群转移类型
     * @see io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent.InType
     */
    private Integer inType;

    /**
     * 猪群转移类型名
     */
    private String inTypeName;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    private Integer source;

    /**
     * 性别 1:混合 2:母 3:公
     * @see io.terminus.doctor.event.model.DoctorGroup.Sex
     */
    private Integer sex;

    private Integer breedId;

    private String breedName;

    private Long fromBarnId;

    private String fromBarnName;

    private Long toBarnId;

    private String toBarnName;

    private Long fromGroupId;

    private String fromGroupCode;

    /**
     * 母猪id
     */
    private Long sowPigId;

    /**
     * 母猪胎次
     */
    private Integer sowParity;

    private Integer boarQty;

    private Integer sowQty;

    public enum InType {
        PIGLET(1, "仔猪转入"),
        SEED(2, "种猪转商品猪"),
        GROUP(3, "群间转移"),
        BUY(4, "购买");

        @Getter
        private Integer value;
        @Getter
        private String desc;

        InType(Integer value, String desc){
            this.value = value;
            this.desc = desc;
        }

        public static InType from(Integer value){
            for (InType type : InType.values()){
                if(Objects.equals(value, type.getValue())){
                    return type;
                }
            }
            return null;
        }
    }
}
