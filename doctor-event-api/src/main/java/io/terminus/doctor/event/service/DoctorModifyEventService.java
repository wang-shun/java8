package io.terminus.doctor.event.service;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.admin.PigEventDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.handler.PigEventHandler;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;

/**
 * Created by xjn on 17/4/14.
 * 编辑与回滚处理
 */
public interface DoctorModifyEventService {

    /**
     * 编辑猪事件
     *
     * @param inputDto  编辑输入
     * @param eventId   事件id
     * @param eventType 事件类型
     * @return 是否编辑成功
     */
    RespWithEx<Boolean> modifyPigEvent(BasePigEventInputDto inputDto, Long eventId, Integer eventType);

    /**
     * 编辑猪事件
     *
     * @param input     编辑输入
     * @param eventId   事件id
     * @param eventType 事件类型
     * @return 是否编辑成功
     */
    RespWithEx<Boolean> modifyGroupEvent(BaseGroupInput input, Long eventId, Integer eventType);


    /**
     * 编辑猪事件
     * 运营后台
     * 直接编辑，慎用
     *
     * @param oldPigEvent
     * @param pigEvent
     * @return
     */
    RespWithEx<Boolean> modifyPigEvent(String oldPigEvent, DoctorPigEvent pigEvent, PigEventHandler pigEventHandler);


    /**
     * 编辑猪群事件
     * 运营后台
     * 直接编辑，慎用
     *
     * @param oldPigEvent
     * @param groupEvent
     * @return
     */
    RespWithEx<Boolean> modifyGroupEvent(String oldPigEvent, DoctorGroupEvent groupEvent);
}
