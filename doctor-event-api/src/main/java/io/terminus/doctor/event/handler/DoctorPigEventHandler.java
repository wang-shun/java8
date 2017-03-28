package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;

import java.util.List;

/**
 * Created by xjn.
 * Date:2017-1-3
 */
public interface DoctorPigEventHandler {

    /**
     * 构建需要执行的猪事件
     * @param basic 基础数据
     * @param inputDto 输入信息
     * @return 猪事件
     */
    DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto);

    /**
     * 构建事件执行后track
     * @param executeEvent 需要执行事件
     * @param fromTrack 执行前track
     * @return 事件执行后track
     */
    DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack);

    /**
     *  创建猪跟踪和镜像表
     *  @param toTrack 事件发生导致track
     *  @param executeEvent 发生事件
     *  @param lastEventId 上一次事件id
     *
     */
    void createPigSnapshot(DoctorPigTrack toTrack, DoctorPigEvent executeEvent, Long lastEventId);

    /**
     * 校验输入数据
     * @param executeEvent 需要执行的事件
     * @param fromTrack 原状态
     */
    void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack);

    /**
     * 事件信息处理
     * @param executeEvent 需要执行的事件
     * @param fromTrack 原状态
     */
    void handle(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack fromTrack);

    /**
     * 通过猪事件构建其触发猪群事件的输入
     * @param pigEvent 猪事件
     * @return 猪群事件输入
     */
    BaseGroupInput buildTriggerGroupEventInput(DoctorPigEvent pigEvent);
}
