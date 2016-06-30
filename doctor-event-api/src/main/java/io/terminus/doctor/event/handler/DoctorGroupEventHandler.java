package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;

import javax.validation.Valid;

/**
 * Desc: 猪群事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
public interface DoctorGroupEventHandler {

    /**
     * 处理猪群事件的接口
     * @param group         猪群
     * @param groupTrack    猪群跟踪
     * @param input         事件录入信息
     * @param <I>           录入信息继承自 BaseGroupInput
     */
    <I extends BaseGroupInput> void handle(DoctorGroup group, DoctorGroupTrack groupTrack, @Valid I input);

    /**
     * 编辑猪群事件的接口
     */
    void edit();
}
