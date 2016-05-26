package io.terminus.doctor.event.model;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪舍表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorBarn implements Serializable {
    private static final long serialVersionUID = -4764581389653266924L;

    private Long id;
    
    /**
     * 猪舍名称
     */
    private String name;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 公司名称
     */
    private String orgName;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 猪场名称
     */
    private String farmName;
    
    /**
     * 猪类名称 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;
    
    /**
     * 能否建群 -1:不能, 1:能
     * @see DoctorBarn.CanOpenGroup
     */
    private Integer canOpenGroup;
    
    /**
     * 使用状态 0:未用 1:在用 -1:已删除
     * @see DoctorBarn.Status
     */
    private Integer status;
    
    /**
     * 猪舍容量
     */
    private Integer capacity;
    
    /**
     * 工作人员id
     */
    private Long staffId;
    
    /**
     * 工作人员name
     */
    private String staffName;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;

    public enum CanOpenGroup {
        NO(-1, "不能"),
        YES(1, "能");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        CanOpenGroup(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static CanOpenGroup from(int number) {
            for (CanOpenGroup canOpenGroup : CanOpenGroup.values()) {
                if (Objects.equal(canOpenGroup.value, number)) {
                    return canOpenGroup;
                }
            }
            return null;
        }
    }

    public enum Status {
        NOUSE(0, "未用"),
        USING(1, "在用"),
        CLOSE(-1, "已删除");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        Status(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Status from(int number) {
            for (Status status : Status.values()) {
                if (Objects.equal(status.value, number)) {
                    return status;
                }
            }
            return null;
        }
    }
}
