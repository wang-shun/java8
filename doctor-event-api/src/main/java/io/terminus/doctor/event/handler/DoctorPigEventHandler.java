package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;

import java.util.List;

/**
 * Created by xjn.
 * Date:2017-1-3
 */
public interface DoctorPigEventHandler {

    /**
     * 校验输入数据
     */
    void handleCheck(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic);

    /**
     * 事件信息处理
     */
    void handle(List<DoctorEventInfo> doctorEventInfoList, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic);

//    /**
//     * after handler
//     */
//    List<DoctorPublishEventDto> publishEvent(List<DoctorEventInfo> doctorEventInfoList);
}
