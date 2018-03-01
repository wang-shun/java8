package io.terminus.doctor.event.reportBi.listener;

import io.terminus.doctor.event.enums.OrzDimension;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by xjn on 18/1/10.
 * email:xiaojiannan@terminus.io
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorReportBiReaTimeEvent {


    /**
     * 消息ID，用于链路跟踪
     */
    private String messageId;

    private Long orzId;

    /**
     * 组织类型
     *
     * @see io.terminus.doctor.event.enums.OrzDimension
     */
    private Integer orzType;

    public DoctorReportBiReaTimeEvent(Long orzId, String messageId) {
        this(messageId, orzId, OrzDimension.ORG.getValue());
    }

}
