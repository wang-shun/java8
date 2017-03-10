package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.NotNull;
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
     * @see io.terminus.doctor.event.model.DoctorGroupTrack.Sex
     */
    private Integer sex;

    private Long breedId;

    private String breedName;

    /**
     * 群间转移来源猪舍信息
     */
    private Long fromBarnId;

    private String fromBarnName;

    private Integer fromBarnType;

    /**
     * 群间转移来源猪群信息
     */
    private Long fromGroupId;

    private String fromGroupCode;

    /**
     * 母猪id
     */
    private Long sowPigId;

    /**
     * 母猪code
     */
    private String sowCode;

    /**
     * 转入数量
     */
    private Integer quantity;
    /**
     * 母猪胎次
     */
    private Integer sowParity;

    private Integer boarQty;

    private Integer sowQty;

    /**
     * 平均日龄
     */
    @NotNull(message = "avgDayAge.not.null")
    private Integer avgDayAge;

    /**
     * 平均体重(单位:kg)
     */
    @NotNull(message = "avgWeight.not.null")
    private Double avgWeight;

    /**
     * 金额(分)
     */
    private Long amount;



    /**
     * 健仔数
     */
    private Integer healthyQty;

    /**
     * 弱仔数
     */
    private Integer weakQty;

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

        public static InType from(String desc){
            for (InType type : InType.values()){
                if(Objects.equals(desc, type.desc)){
                    return type;
                }
            }
            return null;
        }
    }
}
