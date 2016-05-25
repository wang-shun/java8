package io.terminus.doctor.web.front.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.web.front.event.dto.DoctorCreateGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorNewGroupDto;

import javax.validation.Valid;

/**
 * Desc: 猪群相关web接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public interface DoctorGroupWebService {

    /**
     * 新建猪群
     * @param newGroupDto 新建猪群所需字段
     * @return 猪群id
     */
    Response<Long> createNewGroup(@Valid DoctorNewGroupDto newGroupDto);

    /**
     * 录入猪群事件
     * @param createEventDto 录入事件所需字段
     * @return 是否成功
     */
    Response<Boolean> createGroupEvent(@Valid DoctorCreateGroupEventDto createEventDto);
}
