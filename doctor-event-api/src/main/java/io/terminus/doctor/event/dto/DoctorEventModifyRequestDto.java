package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/3/14.
 * 对编辑事件请求的扩展便于显示
 */
@Data
public class DoctorEventModifyRequestDto extends DoctorEventModifyRequest implements Serializable{

    private static final long serialVersionUID = -6307796229125367593L;

    /**
     * 原猪事件
     */
    private DoctorPigEvent oldPigEvent;

    /**
     * 新猪事件
     */
    private DoctorPigEvent newPigEvent;

    /**
     * 原猪群事件
     */
    private DoctorGroupEvent oldGroupEvent;

    /**
     * 新猪群事件
     */
    private DoctorGroupEvent newGroupEvent;
}
