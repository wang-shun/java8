package io.terminus.doctor.basic.model;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 基础数据表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-24
 */
@Data
public class DoctorBasic implements Serializable {
    private static final long serialVersionUID = -7551781912423709587L;

    private Long id;
    
    /**
     * 基础数据内容
     */
    @NotEmpty(message = "name.not.empty")
    private String name;
    
    /**
     * 基础数据类型 枚举
     * @see Type
     */
    @NotNull(message = "type.not.empty")
    private Integer type;
    
    /**
     * 数据类型名称
     */
    private String typeName;

    /**
     * 逻辑删除字段, -1 无效数据, 1 有效数据
     */
    private Integer isValid;

    /**
     * 输入码(快捷输入用)
     */
    private String srm;

    /**
     * 基础数据内容
     */
    private String context;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 附加字段
     */
    private String extra;
    
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

    public enum Type {
        BREED(1, "品种"),
        GENETICS(2, "品系"),
        ANTIEPIDEMIC(3, "防疫项目"),
        DISEASE(4, "疾病"),
        UNIT(5, "计量单位"),
        FOSTER_REASON(6, "寄养原因"),
        CHANGE_TYPE(7, "变动类型"),
        ABORTION_REASON(8, "流产原因"),

        // 物料相关
        FEED_SUB(11, "物料—饲料—子类"),
        MATERIAL_SUB(12, "物料—原料—子类"),
        VACCINATION_SUB(13, "物料—疫苗—子类"),
        MEDICINE_SUB(14, "物料—药品—子类"),
        CONSUME_SUB(15, "物料—消耗品—子类");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        Type(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Type from(int number) {
            for (Type type : Type.values()) {
                if (Objects.equal(type.value, number)) {
                    return type;
                }
            }
            return null;
        }

        public static Type from(String desc) {
            for (Type type : Type.values()) {
                if (Objects.equal(type.desc, desc)) {
                    return type;
                }
            }
            return null;
        }
    }
}
