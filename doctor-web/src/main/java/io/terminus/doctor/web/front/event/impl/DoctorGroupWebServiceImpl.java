package io.terminus.doctor.web.front.event.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.DoctorNewGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

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
    private final DoctorGroupReadService doctorGroupReadService;

    @Autowired
    public DoctorGroupWebServiceImpl(DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorFarmReadService doctorFarmReadService,
                                     DoctorBasicReadService doctorBasicReadService,
                                     DoctorBarnReadService doctorBarnReadService,
                                     DoctorGroupReadService doctorGroupReadService) {
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public Response<Long> createNewGroup(DoctorNewGroupInput newGroupInput) {
        try {
            //1. 构造猪群信息
            DoctorGroup group = getNewGroup(newGroupInput);

            //2. 构造猪群事件
            DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = getNewGroupEvent(newGroupInput, group);

            //3. 构造猪群跟踪信息
            DoctorGroupTrack groupTrack = BeanMapper.map(groupEvent, DoctorGroupTrack.class);

            return doctorGroupWriteService.createNewGroup(group, groupEvent, groupTrack);
        } catch (Exception e) {
            log.error("create new group failed, newGroupInput:{}, cause:{}", newGroupInput, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.create.fail");
        }
    }

    //构造新建猪群信息
    private DoctorGroup getNewGroup(DoctorNewGroupInput newGroupInput) {
        DoctorGroup group = BeanMapper.map(newGroupInput, DoctorGroup.class);

        //设置猪场公司信息
        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(group.getFarmId()));
        group.setFarmName(farm.getName());

        DoctorOrg org = RespHelper.orServEx(doctorFarmReadService.findOrgById(farm.getOrgId()));
        group.setOrgId(org.getId());
        group.setOrgName(org.getName());

        //设置猪舍
        group.setInitBarnId(newGroupInput.getBarnId());
        group.setInitBarnName(newGroupInput.getBarnName());
        group.setCurrentBarnId(newGroupInput.getBarnId());
        group.setCurrentBarnName(newGroupInput.getBarnName());

        //建群时间与状态
        group.setOpenAt(MoreObjects.firstNonNull(group.getOpenAt(), new Date()));
        group.setStatus(DoctorGroup.Status.CREATED.getValue());

        //事件录入人信息
        group.setCreatorId(UserUtil.getUserId());
        group.setCreatorName(UserUtil.getCurrentUser().getName());
        return group;
    }

    //构造新建猪群事件
    private DoctorGroupEvent<DoctorNewGroupEvent> getNewGroupEvent(DoctorNewGroupInput newGroupInput, DoctorGroup group) {
        DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = new DoctorGroupEvent<>();
        groupEvent.setOrgId(group.getOrgId());
        groupEvent.setOrgName(group.getOrgName());
        groupEvent.setFarmId(group.getFarmId());
        groupEvent.setGroupCode(group.getGroupCode());

        //事件信息
        groupEvent.setEventAt(group.getOpenAt());
        groupEvent.setType(GroupEventType.NEW.getValue());
        groupEvent.setName(GroupEventType.NEW.getDesc());
        groupEvent.setDesc("todo 事件描述");

        groupEvent.setBarnId(group.getInitBarnId());
        groupEvent.setBarnName(group.getInitBarnName());
        groupEvent.setPigType(group.getPigType());

        groupEvent.setCreatorId(group.getCreatorId());
        groupEvent.setCreatorName(group.getCreatorName());
        groupEvent.setRemark(group.getRemark());

        DoctorNewGroupEvent newGroupEvent = new DoctorNewGroupEvent();
        newGroupEvent.setType(GroupEventType.NEW.getValue());
        newGroupEvent.setSource(newGroupInput.getSource());
        groupEvent.setExtraMap(newGroupEvent);

        return groupEvent;
    }

    @Override
    public Response<Boolean> createGroupEvent(Long groupId, Integer eventType, Map<String, Object> params) {
        try {
            //1.校验猪群是否存在
            DoctorGroupDetail groupDetail = checkGroupExist(groupId);

            //2.校验能否操作此事件
            // TODO: 16/5/26

            params.put("isAuto", IsOrNot.NO.getValue());
            params.put("creatorId", UserUtil.getUserId());
            params.put("creatorName", UserUtil.getCurrentUser().getName());

            //根据不同的事件类型调用不同的录入接口
            GroupEventType groupEventType = checkNotNull(GroupEventType.from(eventType));
            switch (groupEventType) {
                case MOVE_IN:
                    RespHelper.or500(doctorGroupWriteService.groupEventMoveIn(groupDetail, BeanMapper.map(params, DoctorMoveInGroupInput.class)));
                    break;
                case CHANGE:
                    RespHelper.or500(doctorGroupWriteService.groupEventChange(groupDetail, BeanMapper.map(params, DoctorChangeGroupInput.class)));
                    break;
                case TRANS_GROUP:
                    RespHelper.or500(doctorGroupWriteService.groupEventTransGroup(groupDetail, BeanMapper.map(params, DoctorTransGroupInput.class)));
                    break;
                case TURN_SEED:
                    RespHelper.or500(doctorGroupWriteService.groupEventTurnSeed(groupDetail, BeanMapper.map(params, DoctorTurnSeedGroupInput.class)));
                    break;
                case LIVE_STOCK:
                    RespHelper.or500(doctorGroupWriteService.groupEventLiveStock(groupDetail, BeanMapper.map(params, DoctorLiveStockGroupInput.class)));
                    break;
                case DISEASE:
                    RespHelper.or500(doctorGroupWriteService.groupEventDisease(groupDetail, BeanMapper.map(params, DoctorDiseaseGroupInput.class)));
                    break;
                case ANTIEPIDEMIC:
                    RespHelper.or500(doctorGroupWriteService.groupEventAntiepidemic(groupDetail, BeanMapper.map(params, DoctorAntiepidemicGroupInput.class)));
                    break;
                case TRANS_FARM:
                    RespHelper.or500(doctorGroupWriteService.groupEventTransFarm(groupDetail, BeanMapper.map(params, DoctorTransFarmGroupInput.class)));
                    break;
                case CLOSE:
                    RespHelper.or500(doctorGroupWriteService.groupEventClose(groupDetail, BeanMapper.map(params, DoctorCloseGroupInput.class)));
                    break;
                default:
                    return Response.fail("event.type.error");
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create group event failed, groupId:{}, eventType:{}, params:{}, cause:{}",
                    groupId, eventType, params, Throwables.getStackTraceAsString(e));
            return Response.fail("create.group.event.fail");
        }
    }

    //校验猪群是否存在
    private DoctorGroupDetail checkGroupExist(Long groupId) {
        return checkNotNull(RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId)), "group.not.exist");
    }
}
