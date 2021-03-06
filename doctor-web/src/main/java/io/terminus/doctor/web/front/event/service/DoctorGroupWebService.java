package io.terminus.doctor.web.front.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.web.front.event.dto.DoctorBatchGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorBatchNewGroupEventDto;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪群相关web接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public interface DoctorGroupWebService {

    /**
     * 新建猪群(此事件单独拿出来)
     * @param newGroupDto 新建猪群所需字段
     * @return 猪群id
     */
    RespWithEx<Long> createNewGroup(DoctorNewGroupInput newGroupDto);

    /**
     * 录入猪群事件
     * @param groupId 猪群id
     * @param eventType 事件类型
     * @param data 入参
     * @see io.terminus.doctor.event.dto.event.group.input.BaseGroupInput
     * @return 是否成功
     */
    RespWithEx<Boolean> createGroupEvent(@NotNull(message = "groupId.not.null") Long groupId,
                                       @NotNull(message = "eventType.not.null") Integer eventType,
                                       @NotEmpty(message = "data.not.empty") String data);
    /**
     * 生成猪群号 猪舍名(yyyy-MM-dd)
     * @param barnName 猪舍名称
     * @return  猪群号
     */
    Response<String> generateGroupCode(String barnName);

    /**
     * 根据猪舍id生成猪群号(主要用于分娩舍: 如果当前猪舍存在猪群直接返回此猪群号, 如果不存在, 新生成猪群号)
     * @param barnId 猪舍id
     * @return 猪群号
     */
    Response<String> generateGroupCode(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 获取用户的真实姓名
     * @param userId
     * @return
     */
    Response<String> findRealName(Long userId);

    /**
     * 批量新建猪群
     * @param batchNewGroupEventDto 批量新建信息
     * @return
     */
    RespWithEx<Boolean> batchNewGroupEvent(DoctorBatchNewGroupEventDto batchNewGroupEventDto);

    /**
     * 批量事件(出去新建猪群)
     * @param batchGroupEventDto 批量事件信息
     * @return
     */
    RespWithEx<Boolean> batchGroupEvent(DoctorBatchGroupEventDto batchGroupEventDto);

    /**
     * 处理猪群编辑事件请求
     * @param groupId 猪群id
     * @param eventType 事件类型
     * @param eventId 事件id
     * @param data 事件输入数据
     * @return
     */
    RespWithEx<Boolean> createGroupModifyEventRequest(Long groupId, Integer eventType, Long eventId, String data);
}
