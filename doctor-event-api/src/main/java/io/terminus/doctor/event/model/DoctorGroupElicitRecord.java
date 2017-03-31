package io.terminus.doctor.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Created by xjn on 17/3/30.
 */
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DoctorGroupElicitRecord implements Serializable {
    private static final long serialVersionUID = -8836595073143835698L;


    private Long id;

    private Long farmId;

    private String farmName;

    private Long groupId;

    private String groupCode;

    /**
     * 状态
     * @see Status
     */
    private Integer status;

    /**
     * 原track
     * @see DoctorPigTrack
     */
    private String fromTrack;

    /**
     * 推演后track
     * @see DoctorPigTrack
     */
    private String toTrack;

    /**
     * 错误原因
     */
    private String errorReason;

    /**
     * 记录版本,从1开始
     */
    private Integer version;

    private Date createdAt;

    public enum Status {
        SUCCESS(1, "推演成功"),
        FAIL(-1, "推演失败");

        @Getter
        private Integer key;

        @Getter
        private String desc;

        Status(Integer key, String desc){
            this.key = key;
            this.desc = desc;
        }

        public static Status from(Integer key){
            for(Status status : Status.values()){
                if(Objects.equals(status.getKey(), key)){
                    return status;
                }
            }
            return null;
        }
    }
}
