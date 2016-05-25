package io.terminus.doctor.web.front.event.impl;

import io.terminus.common.model.Response;
import io.terminus.doctor.web.front.event.dto.DoctorCreateGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorNewGroupDto;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
public class DoctorGroupWebServiceImpl implements DoctorGroupWebService {

    @Override
    public Response<Boolean> createNewGroup(DoctorNewGroupDto newGroupDto) {
        return null;
    }

    @Override
    public Response<Boolean> createGroupEvent(DoctorCreateGroupEventDto createEventDto) {
        return null;
    }
}
