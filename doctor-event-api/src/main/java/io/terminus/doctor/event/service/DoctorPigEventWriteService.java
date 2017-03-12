package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.model.DoctorPigEvent;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 猪事件录入信息表
 */
public interface DoctorPigEventWriteService {

    /**
     * 创建事件(临时使用)
     * @param doctorPigEvent
     * @return
     */
    Response<Boolean> createPigEvent(DoctorPigEvent doctorPigEvent);

    /**
     * 更新事件(临时使用)
     * @param doctorPigEvent
     * @return
     */
    Response<Boolean> updatePigEvents(DoctorPigEvent doctorPigEvent);

    /**
     * 更新事件(临时使用)
     * @param doctorPigEvent
     * @return
     */
    Response<Boolean> updatePigEvent(DoctorPigEvent doctorPigEvent);

    /**
     * 猪事件接口
     * @param inputDto
     * @param basic
     * @return
     */
    RespWithEx<Boolean> pigEventHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic);

    /**
     * 批量猪事件接口
     * @param inputDtos
     * @param basic
     * @return
     */
    RespWithEx<Boolean> batchPigEventHandle(List<BasePigEventInputDto> inputDtos, DoctorBasicInputInfoDto basic);
}
