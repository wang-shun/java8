package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;

/**
 * Created by xjn on 17/3/10.
 * 编辑事件写service
 */
public interface DoctorEventModifyRequestWriteService {

    /**
     * 创建事件编辑请求
     *@param modifyRequest 事件编辑请求
     * @return 是否编辑成功
     */
    Response<Boolean> createRequest(DoctorEventModifyRequest modifyRequest);

    /**
     * 创建猪事件编辑的请求
     * @param basic 基础输入
     * @param inputDto 事件输入
     * @param eventId 编辑事件id
     * @param userId 编辑人id
     * @param realName 编辑人真实姓名
     * @return 请求id
     */
    Response<Long> createPigModifyEventRequest(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto, Long eventId, Long userId, String realName);

    /**
     * 创建猪群事件编辑的请求
     * @param eventId 编辑事件id
     * @param userId 编辑人id
     * @param realName 编辑人真实姓名
     * @return 请求id
     */
    Response<Long> createGroupModifyEventRequest(DoctorGroupInputInfo inputInfo, Long eventId, Integer eventType, Long userId, String realName);

    /**
     * 猪事件编辑处理
     * @param modifyRequest 事件编辑请求
     * @return
     */
    RespWithEx<Boolean> modifyPigEventHandle(DoctorEventModifyRequest modifyRequest);
}
