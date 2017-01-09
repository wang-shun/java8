package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPublishEventDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;

import java.util.List;

/**
 * Created by xjn.
 * Date:2017-1-3
 */
public interface DoctorPigEventHandler {

    /**
     * 校验Handler 的处理方式
     */
    void preHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic);

    /**
     * 事件信息处理handler
     */
    void handle(List<DoctorEventInfo> doctorEventInfoList, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic);

    /**
     * after handler
     */
    List<DoctorPublishEventDto> publishEvent(List<DoctorEventInfo> doctorEventInfoList);
}
