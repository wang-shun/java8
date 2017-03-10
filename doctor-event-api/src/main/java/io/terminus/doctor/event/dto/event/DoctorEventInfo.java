package io.terminus.doctor.event.dto.event;

import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
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

    /**
     * 本次处理的事件id
     */
    private Long eventId;

    /**
     * 原事件id,事件编辑时记录编辑前的事件id(仅编辑可用)
     */
    private Long oldEventId;

    /**
     * 事件编辑前猪track(仅编辑可用)
     */
    private DoctorPigTrack fromPigTrack;

    /**
     * 事件编辑前猪群track(仅编辑可用)
     */
    private DoctorPigTrack fromGroupTrack;

    /**
     * 目标号
     */
    private String code;

    /**
     * 处理目标id(猪id、猪群id)
     */
    private Long businessId;

    /**
     * 目标状态
     */
    private Integer status;

    /**
     * 事件时间
     */
    private Date eventAt;

    /**
     * 猪事件类型
     * @see io.terminus.doctor.event.enums.PigEvent
     * 猪群事件类型
     * @see io.terminus.doctor.event.enums.GroupEventType
     */
    private Integer eventType;

    /**
     * 目标类型
     * @see Business_Type
     */
    private Integer businessType;
    /**
     * 猪舍类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;

    /**
     * 如果是猪事件,猪的类型(性别)
     * @see DoctorPig.PigSex
     */

    private Integer kind;
    /**
     * 配种类型(如果是配种事件)
     * @see io.terminus.doctor.event.enums.DoctorMatingType
     */
    private Integer mateType;

    /**
     * 妊检结果(如果是妊检事件)
     * @see io.terminus.doctor.event.enums.PregCheckResult
     */
    private Integer pregCheckResult;

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
