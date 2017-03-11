package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;

import javax.validation.Valid;
import java.util.List;

/**
 * Desc: 猪群事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
public interface DoctorGroupEventHandler {

    /**
     * 构建猪群事件
     * @param group         猪群
     * @param groupTrack    猪群跟踪
     * @param input         事件录入信息
     * @param <I>           录入信息继承自 BaseGroupInput
     */
    <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, @Valid I input);

    /**
     * 处理猪群事件的接口
     * @param eventInfoList 事件信息列表
     * @param group         猪群
     * @param groupTrack    猪群跟踪
     * @param input         事件录入信息
     * @param <I>           录入信息继承自 BaseGroupInput
     */
    <I extends BaseGroupInput> void handle(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, @Valid I input);

    int getGroupAvgDayAge(Long groupId, DoctorGroupEvent doctorGroupEvent);
}
