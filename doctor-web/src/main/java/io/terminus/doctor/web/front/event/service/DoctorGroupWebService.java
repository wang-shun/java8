package io.terminus.doctor.web.front.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

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
    Response<Long> createNewGroup(@Valid DoctorNewGroupInput newGroupDto);

    /**
     * 录入猪群事件
     * @param groupId 猪群id
     * @param eventType 事件类型
     * @param params 入参
     * @see io.terminus.doctor.event.dto.event.group.input.BaseGroupInput
     * @return 是否成功
     */
    Response<Boolean> createGroupEvent(@NotNull(message = "groupId.not.null") Long groupId,
                                       @NotNull(message = "eventType.not.null") Integer eventType,
                                       Map<String, Object> params);
}
