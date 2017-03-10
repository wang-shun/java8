package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
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
}
