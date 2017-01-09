package io.terminus.doctor.event.dto.event;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Builder;

import java.util.Date;
import java.util.Objects;

/**
 * Created by xjn on 17/1/4.
 * 事件信息
 */
@Data
@Builder
public class DoctorEventInfo {
    private Long orgId;
    private Long farmId;
    private Long eventId;
    private String code;
    private Long businessId;
    private Integer status;
    private Date eventAt;
    private Integer eventType;
    private Integer businessType;

    public enum Business_Type {
        PIG(1, "猪"),
        GROUP(2, "猪群");

        Business_Type(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        @Getter
        private Integer value;

        @Getter
        private String desc;

        public Business_Type from (Integer value){
            for(Business_Type businessType : Business_Type.values()) {
                if (Objects.equals(value, businessType.getValue())) {
                    return businessType;
                }
            }
            return null;
        }
    }
}
