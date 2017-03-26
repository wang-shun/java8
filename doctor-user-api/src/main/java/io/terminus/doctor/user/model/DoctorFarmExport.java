package io.terminus.doctor.user.model;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/3/23.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFarmExport implements Serializable {
    private static final long serialVersionUID = -7317297983446734024L;

    private Long id;

    private Long farmId;
    /**
     * 导入猪场名
     */
    private String farmName;
    /**
     * 导入文件地址
     */
    private String url;

    /**
     * 导入的状态
     */
    private Integer status;
    private Date createdAt;
    private Date updatedAt;

    public enum Status {
        SUCCESS(1, "成功"),
        FAILED(0, "失败");

        @Getter
        private Integer value;
        @Getter
        private String desc;

        Status(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Status from(Integer value) {
            for (Status status : Status.values()) {
                if (Objects.equal(value, status.getValue())) {
                    return status;
                }
            }
            return null;
        }
    }
}
