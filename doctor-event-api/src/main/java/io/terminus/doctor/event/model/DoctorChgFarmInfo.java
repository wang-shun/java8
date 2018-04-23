package io.terminus.doctor.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 18/4/20.
 * email:xiaojiannan@terminus.io
 * 用于记录在原猪场时的信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorChgFarmInfo implements Serializable {
    private static final long serialVersionUID = 4072321777263676445L;

    private Long id;

    private Long farmId;

    private Long pigId;

    private String pigCode;

    /**
     * 猪性别
     * @see DoctorPig.PigSex
     */
    private Integer pigType;

    private Long barnId;

    private String rfid;

    /**
     * 转场id
     */
    private Long eventId;

    private String track;

    private String pig;

    private Date createdAt;

    private Date updatedAt;
}
