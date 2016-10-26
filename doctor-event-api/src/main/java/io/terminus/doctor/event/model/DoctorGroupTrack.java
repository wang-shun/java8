package io.terminus.doctor.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import io.terminus.common.utils.JsonMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

import static java.util.Objects.isNull;

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
     * 总活体重(公斤)
     */
    private Double weight;
    
    /**
     * 平均体重(公斤)
     */
    private Double avgWeight;

    /**
     * 断奶均重kg
     */
    private Double weanAvgWeight;

    /**
     * 出生均重kg
     */
    private Double birthAvgWeight;

    /**
     * 弱仔数
     */
    private Integer weakQty;

    /**
     * 未断奶数
     */
    private Integer unweanQty;

    /**
     * 合格数(需求变更，只需要合格数了，这里翻一下)
     */
    private Integer unqQty;
    
    /**
     * 单价(分)
     */
    private Long price;
    
    /**
     * 总金额(分)
     */
    private Long amount;
    
    /**
     * 客户id
     */
    private Long customerId;
    
    /**
     * 客户名称
     */
    private String customerName;
    
    /**
     * 销售数量
     */
    private Integer saleQty;
    
    /**
     * 附加字段
     */
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extra;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Extra extraEntity;
    
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

    @SneakyThrows
    public void setExtra(String extra) {
        this.extra = Strings.nullToEmpty(extra);
        if (Strings.isNullOrEmpty(extra)) {
            this.extraEntity = new Extra();
        } else {
            this.extraEntity = JsonMapper.nonEmptyMapper().fromJson(extra, Extra.class);
        }
    }

    @SneakyThrows
    public void setExtraEntity(Extra extraEntity){
        this.extraEntity = extraEntity;
        if (isNull(extraEntity)) {
            this.extra = "";
        } else {
            this.extra = JsonMapper.nonEmptyMapper().toJson(extraEntity);
        }
    }

    @SneakyThrows
    public Extra getExtraEntity() {
        if (Strings.isNullOrEmpty(this.extra)) {
            return new Extra();
        }
        return JsonMapper.nonEmptyMapper().fromJson(this.extra, Extra.class);
    }

    /**
     * track 的 extra字段
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Extra implements Serializable {
        private static final long serialVersionUID = -4134534768021946700L;
        private Date newAt;          //新建猪群时间
        private Date moveInAt;       //转入猪群时间
        private Date changeAt;       //猪群变动时间
        private Date transGroupAt;   //猪群转群时间
        private Date turnSeedAt;     //商品猪转为种猪时间
        private Date liveStockAt;    //猪只存栏时间
        private Date diseaseAt;      //疾病时间
        private Date antiepidemicAt; //防疫时间
        private Date transFarmAt;    //转场时间
        private Date closeAt;        //关闭猪群时间
    }
}
