package io.terminus.doctor.event.service;

import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorPigEvent;

/**
 * Created by xjn on 17/3/12.
 * 猪事件编辑
 */
public interface DoctorEditPigEventService {
    /**
     * 猪事件编辑处理
     * @param modifyRequest 事件编辑请求
     * @return
     */
    RespWithEx<Boolean> modifyPigEventHandle(DoctorEventModifyRequest modifyRequest);

    /**
     * 猪事件编辑处理
     * @param modifyEvent 编辑的事件
     * @return
     */
    RespWithEx<Boolean> modifyPigEventHandle(DoctorPigEvent modifyEvent);
}
