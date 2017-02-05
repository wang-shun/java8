package io.terminus.doctor.event.model;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群卡片明细表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorGroupTrack implements Serializable {
    private static final long serialVersionUID = -423032174027191008L;

    private Long id;
    
    /**
     * 猪群卡片id
     */
    private Long groupId;
    
    /**
     * 关联的最新一次的事件id
     */
    private Long relEventId;

    /**
     * 性别 0母 1公 2混合
     * @see io.terminus.doctor.event.model.DoctorGroupTrack.Sex
     */
    private Integer sex;

    /**
     * 猪只数
     */
    private Integer quantity;

    /**
     * 公猪数
     */
    private Integer boarQty;

    /**
     * 母猪数
     */
    private Integer sowQty;

    /**
     * 出生日期(此日期仅用于计算日龄)
     */
    private Date birthDate;

    /**
     * 平均日龄
     */
    private Integer avgDayAge;

    /**
     * 断奶重kg
     */
    private Double weanWeight;

    /**
     * 出生重kg
     */
    private Double birthWeight;

    /**
     * 窝数(分娩时累加)
     */
    private Integer nest;

    /**
     * 活仔数(分娩时累加)
     */
    private Integer liveQty;

    /**
     * 健仔数(分娩时累加)
     */
    private Integer healthyQty;

    /**
     * 弱仔数
     */
    private Integer weakQty;

    /**
     * 未断奶数
     */
    private Integer unweanQty;

    /**
     * 断奶数(断奶时累加)
     */
    private Integer weanQty;

    /**
     * 合格数
     */
    private Integer quaQty;

    /**
     * 不合格数
     */
    private Integer unqQty;

    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人name
     */
    private String creatorName;
    
    /**
     * 更新人id
     */
    private Long updatorId;
    
    /**
     * 更新人name
     */
    private String updatorName;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;

    public enum Sex {
        FEMALE(0, "母"),
        MALE(1, "公"),
        MIX(2, "混合");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        Sex(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Sex from(int number) {
            for (Sex sex : Sex.values()) {
                if (Objects.equal(sex.value, number)) {
                    return sex;
                }
            }
            return null;
        }

        public static Sex from(String desc) {
            for (Sex sex : Sex.values()) {
                if (Objects.equal(sex.desc, desc)) {
                    return sex;
                }
            }
            return null;
        }
    }
}
