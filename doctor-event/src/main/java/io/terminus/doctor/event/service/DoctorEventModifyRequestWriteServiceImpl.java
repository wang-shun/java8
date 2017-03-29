package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.enums.EventRequestStatus;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.helper.DoctorMessageSourceHelper;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorMessageSourceHelper messageSourceHelper;
    @Autowired(required = false)
    private Publisher publisher;

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
            if (Objects.equals(eventId, IsOrNot.YES)) {
                throw new ServiceException("event.not.allow.modify");
            }
            DoctorPigEvent oldEvent = doctorPigEventDao.findById(eventId);
            DoctorPigEvent modifyEvent = pigEventManager.buildPigEvent(basic, inputDto);
            modifyEvent.setGroupId(oldEvent.getGroupId());
            modifyEvent.setIsModify(IsOrNot.YES.getValue());
            modifyEvent.setId(eventId);
            log.info("build modifyEvent, modifyEvent = {}", modifyEvent);

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
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create request failed, basic:{}, inputDto:{}, eventId:{}, userId:{}, userName:{}, cause:{}", basic, inputDto, eventId, userId, realName, Throwables.getStackTraceAsString(e));
            return Response.fail("create.pig.modify.event.request.failed");
        }
    }

    @Override
    public Response<Long> createGroupModifyEventRequest(DoctorGroupInputInfo inputInfo, Long eventId, Integer eventType, Long userId, String realName) {

        try {
            if (Objects.equals(eventId, IsOrNot.YES)) {
                throw new ServiceException("event.not.allow.modify");
            }
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
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create request failed, inputInfo:{}, eventId:{}, eventType:{}, userId:{}, userName:{}, cause:{}", inputInfo, eventId, eventType, userId, realName, Throwables.getStackTraceAsString(e));
            return Response.fail("create.group.modify.event.request.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> modifyEventHandle(DoctorEventModifyRequest modifyRequest) {
        try {
            List<DoctorEventModifyRequest> handlingList = eventModifyRequestDao.listByStatus(EventRequestStatus.HANDLING.getValue());
            if (handlingList.isEmpty()) {
                modifyEventRequestHandleImpl(modifyRequest);
            }
        } catch (Exception e) {
            log.error("modify.pig.event.handle.failed, modifyRequest:{}, cause by :{}", modifyRequest, Throwables.getStackTraceAsString(e));
        }
        return RespWithEx.ok(Boolean.TRUE);

    }

    @Override
    public Response<Boolean> batchUpdateStatus(List<Long> ids, Integer status) {
        try {
            if (Arguments.isNullOrEmpty(ids)) {
                return Response.ok(Boolean.TRUE);
            }
            return Response.ok(eventModifyRequestDao.batchUpdateStatus(ids, status));
        } catch (Exception e) {
            log.error("batch update status failed, ids:{}, status:{}, cause:{}", ids, status, Throwables.getStackTraceAsString(e));
            return Response.fail("batch.update.status.failed");
        }
    }

    @Override
    public Response<Boolean> modifyRequestHandleJob(List<DoctorEventModifyRequest> requestList) {
        try {
            if (Arguments.isNullOrEmpty(requestList)) {
                return Response.ok(Boolean.TRUE);
            }
            eventModifyRequestDao.batchUpdateStatus(requestList.stream().map(DoctorEventModifyRequest::getId).collect(Collectors.toList()), EventRequestStatus.HANDLING.getValue());
            requestList.forEach(this::modifyEventRequestHandleImpl);

            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("modify request handle job failed, requestList:{}, cause:{}", requestList, Throwables.getStackTraceAsString(e));
            return Response.fail("modify.request.handle.job.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> elicitPigTrack(@NotNull(message = "pig.id.not.null") Long pigId) {
        try {
            doctorEditPigEventService.elicitPigTrack(pigId);
            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            log.error("elicit pig track failed, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        }catch (ServiceException e) {
            log.error("elicit pig track failed, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("elicit pig track failed, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("elicit.pig.track.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> batchElicitPigTrack(@NotNull(message = "farm.id.not.null") Long farmId) {
        try {
            List<Long> pigIdList = doctorPigDao.findPigIdsByFarmId(farmId);
            if (pigIdList.isEmpty()) {
                return RespWithEx.ok(Boolean.TRUE);
            }
            pigIdList.forEach(pigId -> {
                doctorEditPigEventService.elicitPigTrack(pigId);
            });
            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            log.error("batch elicit pig track failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        }catch (ServiceException e) {
            log.error("batch elicit pig track failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("batch elicit pig track failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("elicit.pig.track.failed");
        }
    }

    /**
     * 通过编辑事件请求处理猪事件编辑请求
     *
     * @param modifyRequest 编辑事件请求
     */
    private boolean modifyEventRequestHandleImpl(DoctorEventModifyRequest modifyRequest) {
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
                doctorEditGroupEventService.elicitDoctorGroupTrackRebuildOne(modifyEvent);  //只重新生成修改的事件,然后推导track
            }

            //更新修改请求的状态
            modifyRequest.setStatus(EventRequestStatus.SUCCESS.getValue());
            eventModifyRequestDao.update(modifyRequest);
            return true;
        } catch (InvalidException e) {
            log.info("modify event request handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            modifyRequest.setStatus(EventRequestStatus.FAILED.getValue());
            modifyRequest.setReason(messageSourceHelper.getMessage(e.getError(), e.getParams()));
            eventModifyRequestDao.update(modifyRequest);
        } catch (Exception e) {
            log.info("modify event request handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            modifyRequest.setStatus(EventRequestStatus.FAILED.getValue());
            modifyRequest.setReason(messageSourceHelper.getMessage("modify.event.request.handle.failed"));
            modifyRequest.setErrorStack(Throwables.getStackTraceAsString(e));
            eventModifyRequestDao.update(modifyRequest);
        }
        log.info("modify event handle ending");
        return false;
    }
// TODO: 17/3/17 刷新报表 
//    private void publishReport(List<DoctorEventModifyRequest> requestList) {
//        Map<Long, List<DoctorEventModifyRequest>> farmMap = requestList.stream()
//                .collect(Collectors.groupingBy(DoctorEventModifyRequest::getFarmId));
//        farmMap.keySet().forEach(farmId -> {
//            List<Long> eventIdList = farmMap.get(farmId).stream().map(DoctorEventModifyRequest::getEventId).collect(Collectors.toList());
//            
//            try {
//                publisher.publish(DataEvent.);
//            }
//        });
//
//    }
}
