package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.enums.EventRequestStatus;
import io.terminus.doctor.event.helper.DoctorMessageSourceHelper;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
    @Autowired
    private DoctorEditPigEventService doctorEditPigEventService;
    @Autowired
    private DoctorEditGroupEventService doctorEditGroupEventService;

    @Autowired
    private DoctorMessageSourceHelper messageSourceHelper;

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
            modifyEvent.setExtraMap(null);
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
            String extra = modifyEvent.getExtra();
            //将extraMap置null,往外拿的时候不会报错
            modifyEvent.setExtraMap(null);
            modifyEvent.setExtra(extra);
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

    @Override
    public RespWithEx<Boolean> modifyPigEventHandle(DoctorEventModifyRequest modifyRequest) {
        try {
            modifyEventRequestHandleImpl(modifyRequest);
        } catch (Exception e) {
            log.error("modify.pig.event.handle.failed, modifyRequest:{}, cause by :{}", modifyRequest, Throwables.getStackTraceAsString(e));
        }
        return RespWithEx.ok(Boolean.TRUE);

    }

    /**
     * 通过编辑事件请求处理猪事件编辑请求
     *
     * @param modifyRequest 编辑事件请求
     */
    private void modifyEventRequestHandleImpl(DoctorEventModifyRequest modifyRequest) {
        log.info("modify event handle starting, modifyRequest:{}", modifyRequest);
        try {
            modifyRequest.setStatus(EventRequestStatus.HANDLING.getValue());
            eventModifyRequestDao.update(modifyRequest);


            if (Objects.equals(modifyRequest.getType(), DoctorEventModifyRequest.TYPE.PIG.getValue())) {
                //处理猪事件修改
                DoctorPigEvent modifyEvent = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.fromJson(modifyRequest.getContent(), DoctorPigEvent.class);
                doctorEditPigEventService.modifyPigEventHandle(modifyEvent);
            } else {
                //处理猪群事件修改
                DoctorGroupEvent modifyEvent = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.fromJson(modifyRequest.getContent(), DoctorGroupEvent.class);
                doctorEditGroupEventService.elicitDoctorGroupTrack(modifyEvent);
            }

            //更新修改请求的状态
            modifyRequest.setStatus(EventRequestStatus.SUCCESS.getValue());
            eventModifyRequestDao.update(modifyRequest);
        } catch (InvalidException e) {
            log.info("modify event request handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            modifyRequest.setStatus(EventRequestStatus.FAILED.getValue());
            modifyRequest.setReason(messageSourceHelper.getMessage(e.getError(), e.getParams()));
            eventModifyRequestDao.update(modifyRequest);
        } catch (Exception e) {
            log.info("modify event request handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            modifyRequest.setStatus(EventRequestStatus.FAILED.getValue());
            modifyRequest.setReason(messageSourceHelper.getMessage("modify.event.request.handle.failed"));
            eventModifyRequestDao.update(modifyRequest);
        }
        log.info("modify event handle ending");
    }
}
