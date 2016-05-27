package io.terminus.doctor.event.model;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 回滚记录表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRevertLog implements Serializable {
    private static final long serialVersionUID = 1861449639048681648L;

    private Long id;
    
    /**
     * 回滚类型 1 母猪，2 公猪，3 猪群
     * @see io.terminus.doctor.event.model.DoctorRevertLog.Type
     */
    private Integer type;
    
    /**
     * 回滚前的信息
     */
    private String fromInfo;
    
    /**
     * 回滚后的信息
     */
    private String toInfo;
    
    /**
     * 回滚人id
     */
    private Long reverterId;
    
    /**
     * 回滚人姓名
     */
    private String reverterName;
    
    /**
     * 回滚时间
     */
    private Date createdAt;

    public enum Type {
        SOW(1, "母猪"),
        BOAR(2, "公猪"),
        GROUP(3, "猪群");

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
    }
}
