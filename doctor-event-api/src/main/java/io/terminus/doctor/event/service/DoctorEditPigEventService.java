package io.terminus.doctor.event.service;

import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.model.DoctorPigEvent;

import java.util.List;

/**
 * Created by xjn on 17/3/12.
 * 猪事件编辑
 */
public interface DoctorEditPigEventService {

    /**
     * 猪事件编辑处理
     * @param modifyEvent 编辑的事件
     * @param modifyRequestId 事件编辑请求id
     * @return
     */
    List<DoctorEventInfo> modifyPigEventHandle(DoctorPigEvent modifyEvent, Long modifyRequestId);

    /**
     * 推演猪track
     * @param pigId 猪id
     */
    void elicitPigTrack(Long pigId);
}
