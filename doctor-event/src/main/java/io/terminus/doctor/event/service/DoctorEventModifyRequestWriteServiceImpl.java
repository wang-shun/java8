package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.enums.EventRequestStatus;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xjn on 17/3/10.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorEventModifyRequestWriteServiceImpl implements DoctorEventModifyRequestWriteService{
    @Autowired
    private DoctorEventModifyRequestDao eventModifyRequestDao;
    @Autowired
    private DoctorPigEventManager pigEventManager;
    @Autowired
    private DoctorGroupEventManager groupEventManager;

    @Override
    public Response<Boolean> createRequest(DoctorEventModifyRequest modifyRequest) {
        try {
            modifyRequest.setStatus(EventRequestStatus.WAITING.getValue());
            return Response.ok(eventModifyRequestDao.create(modifyRequest));
        } catch (Exception e) {
            log.error("create request failed, modifyRequest:{}, cause:{}", modifyRequest, Throwables.getStackTraceAsString(e));
            return Response.fail("create.request.failed");
        }
    }

    @Override
    public Response<Long> createPigModifyEventRequest(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto,Long eventId, Long userId, String realName) {
        try {
            DoctorPigEvent modifyEvent = pigEventManager.buildPigEvent(basic, inputDto);
            log.info("build modifyEvent, modifyEvent = {}", modifyEvent);
            modifyEvent.setId(eventId);
            DoctorEventModifyRequest modifyRequest = DoctorEventModifyRequest
                    .builder()
                    .farmId(basic.getFarmId())
                    .businessId(modifyEvent.getPigId())
                    .businessCode(modifyEvent.getPigCode())
                    .eventId(eventId)
                    .status(EventRequestStatus.WAITING.getValue())
                    .type(DoctorEventModifyRequest.TYPE.PIG.getValue())
                    .content(JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.toJson(modifyEvent))
                    .userId(userId)
                    .userName(realName)
                    .build();
            log.info("build modifyRequest, modifyRequest = {}", modifyRequest);
            eventModifyRequestDao.create(modifyRequest);
            return Response.ok(modifyRequest.getId());
        } catch (Exception e) {
            log.error("create request failed, basic:{}, inputDto:{}, eventId:{}, userId:{}, userName:{}, cause:{}", basic, inputDto, eventId, userId, realName, Throwables.getStackTraceAsString(e));
            return Response.fail("create.pig.modify.event.request.failed");
        }
    }

    @Override
    public Response<Long> createGroupModifyEventRequest(DoctorGroupInputInfo inputInfo, Long eventId, Integer eventType, Long userId, String realName) {
        try {
            DoctorGroupEvent modifyEvent = groupEventManager.buildGroupEvent(inputInfo, eventType);
            modifyEvent.setId(eventId);
            DoctorEventModifyRequest modifyRequest = DoctorEventModifyRequest
                    .builder()
                    .farmId(inputInfo.getGroupDetail().getGroup().getFarmId())
                    .businessId(modifyEvent.getGroupId())
                    .businessCode(modifyEvent.getGroupCode())
                    .eventId(eventId)
                    .status(EventRequestStatus.WAITING.getValue())
                    .type(DoctorEventModifyRequest.TYPE.GROUP.getValue())
                    .content(JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.toJson(modifyEvent))
                    .userId(userId)
                    .userName(realName)
                    .build();
            eventModifyRequestDao.create(modifyRequest);
            return Response.ok(modifyRequest.getId());
        } catch (Exception e) {
            log.error("create request failed, inputInfo:{}, eventId:{}, eventType:{}, userId:{}, userName:{}, cause:{}", inputInfo, eventId, eventType, userId, realName, Throwables.getStackTraceAsString(e));
            return Response.fail("create.group.modify.event.request.failed");
        }
    }
}
