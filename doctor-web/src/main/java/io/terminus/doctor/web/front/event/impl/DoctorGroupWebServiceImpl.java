package io.terminus.doctor.web.front.event.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.front.event.dto.DoctorCreateGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorNewGroupDto;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
public class DoctorGroupWebServiceImpl implements DoctorGroupWebService {

    private final DoctorGroupWriteService doctorGroupWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorBasicReadService doctorBasicReadService;
    private final DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorGroupWebServiceImpl(DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorFarmReadService doctorFarmReadService,
                                     DoctorBasicReadService doctorBasicReadService,
                                     DoctorBarnReadService doctorBarnReadService) {
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBarnReadService = doctorBarnReadService;
    }

    @Override
    public Response<Long> createNewGroup(DoctorNewGroupDto newGroupDto) {
        try {
            //1. 构造猪群信息
            DoctorGroup group = BeanMapper.map(newGroupDto, DoctorGroup.class);

            //设置猪场公司信息
            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(group.getFarmId()));
            group.setFarmName(farm.getName());
            DoctorOrg org = RespHelper.orServEx(doctorFarmReadService.findOrgById(farm.getOrgId()));
            group.setOrgId(org.getId());
            group.setOrgName(org.getName());

            //设置猪舍
            group.setInitBarnId(newGroupDto.getBarnId());
            group.setInitBarnName(newGroupDto.getBarnName());
            group.setCurrentBarnId(newGroupDto.getBarnId());
            group.setCurrentBarnName(newGroupDto.getBarnName());

            //建群时间与状态
            group.setOpenAt(MoreObjects.firstNonNull(group.getOpenAt(), new Date()));
            group.setStatus(DoctorGroup.Status.CREATED.getValue());

            //事件录入人信息
            group.setCreatorId(UserUtil.getUserId());
            group.setCreatorName(UserUtil.getCurrentUser().getName());

            // TODO: 16/5/25 雏鹰模式的批次号?

            //2. 构造猪群事件
            DoctorGroupEvent groupEvent = BeanMapper.map(group, DoctorGroupEvent.class);
            groupEvent.setType(GroupEventType.NEW.getValue());
            groupEvent.setName(GroupEventType.NEW.getDesc());
            groupEvent.setDesc("todo 事件描述");
            groupEvent.setEventAt(group.getOpenAt());
            // TODO: 16/5/25 setExtra

            //3. 构造猪群跟踪信息
            DoctorGroupTrack groupTrack = BeanMapper.map(groupEvent, DoctorGroupTrack.class);
            groupTrack.setExtra("todo");

            return doctorGroupWriteService.createNewGroup(group, groupEvent, groupTrack);
        } catch (Exception e) {
            log.error("create new group failed, newGroupDto:{}, cause:{}", newGroupDto, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.create.fail");
        }
    }

    @Override
    public Response<Boolean> createGroupEvent(DoctorCreateGroupEventDto createEventDto) {
        return null;
    }
}
