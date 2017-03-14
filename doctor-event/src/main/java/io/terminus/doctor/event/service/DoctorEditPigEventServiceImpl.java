package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.dao.DoctorEventRelationDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.enums.EventRequestStatus;
import io.terminus.doctor.event.enums.EventStatus;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.helper.DoctorMessageSourceHelper;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;

/**
 * Created by xjn on 17/3/12.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorEditPigEventServiceImpl implements DoctorEditPigEventService {
    @Autowired
    private DoctorPigEventManager doctorPigEventManager;
    @Autowired
    private DoctorPigSnapshotDao doctorPigSnapshotDao;
    @Autowired
    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorEventModifyRequestDao eventModifyRequestDao;
    @Autowired
    private DoctorGroupEventManager doctorGroupEventManager;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorEventRelationDao doctorEventRelationDao;
    @Autowired
    private DoctorEditGroupEventService doctorEditGroupEventService;
    @Autowired
    private DoctorMessageSourceHelper messageSourceHelper;

    private static final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    private static final List<Integer> NOT_MODIFY_EVENT = Lists.newArrayList(PigEvent.CHG_LOCATION.getKey(), PigEvent.CHG_FARM.getKey(), PigEvent.FOSTERS.getKey(), PigEvent.FOSTERS_BY.getKey());

    private static final List<Integer> TRIGGER_GROUP_EVENT = Lists.newArrayList(
            PigEvent.CHG_LOCATION.getKey(), PigEvent.CHG_FARM.getKey(),
            PigEvent.FOSTERS.getKey(), PigEvent.FOSTERS_BY.getKey(),
            PigEvent.FARROWING.getKey(), PigEvent.PIGLETS_CHG.getKey(), PigEvent.WEAN.getKey());

    @Override
    public RespWithEx<Boolean> modifyPigEventHandle(DoctorEventModifyRequest modifyRequest) {
        try {
            modifyPigEventRequestHandleImpl(modifyRequest);
            return RespWithEx.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("modify.pig.event.handle.failed, modifyRequest:{}, cause by :{}", modifyRequest, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("modify.pig.event.handle.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> modifyPigEventHandle(DoctorPigEvent modifyEvent) {
        try {
            modifyPigEventHandleImpl(modifyEvent);
            return RespWithEx.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("modify.pig.event.handle.failed, modifyEvent:{}, cause by :{}", modifyEvent, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("modify.pig.event.handle.failed");
        }
    }

    /**
     * 通过编辑事件请求处理猪事件编辑请求
     * @param modifyRequest 编辑事件请求
     */
    private void modifyPigEventRequestHandleImpl(DoctorEventModifyRequest modifyRequest) {
        log.info("modify pig event handle starting, modifyRequest:{}", modifyRequest);
        try {
            modifyRequest.setStatus(EventRequestStatus.HANDLING.getValue());
            eventModifyRequestDao.update(modifyRequest);

            //处理猪事件修改
            DoctorPigEvent modifyEvent = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.fromJson(modifyRequest.getContent(), DoctorPigEvent.class);
            modifyPigEventHandleImpl(modifyEvent);

            //更新修改请求的状态
            modifyRequest.setStatus(EventRequestStatus.SUCCESS.getValue());
            eventModifyRequestDao.update(modifyRequest);
        } catch (InvalidException e) {
            log.info("modify pig event request handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            modifyRequest.setStatus(EventRequestStatus.FAILED.getValue());
            modifyRequest.setReason(messageSourceHelper.getMessage(e.getError(), e.getParams()));
            eventModifyRequestDao.update(modifyRequest);
            throw e;
        } catch (Exception e) {
            log.info("modify pig event request handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            modifyRequest.setStatus(EventRequestStatus.FAILED.getValue());
            modifyRequest.setReason(messageSourceHelper.getMessage("modify.pig.event.request.handle.failed"));
            eventModifyRequestDao.update(modifyRequest);
            throw e;
        }
        log.info("modify pig event handle ending");
    }

    /**
     * 通过编辑事件调用事件编辑处理
     * @param modifyEvent 编辑事件
     */
    private void modifyPigEventHandleImpl(DoctorPigEvent modifyEvent) {
        log.info("modifyPigEventHandleImpl starting, modifyEvent:{}", modifyEvent);

        List<DoctorEventInfo> doctorEventInfoList = Lists.newArrayList();
        List<Long> pigOldEventIdList = Lists.newLinkedList();
        DoctorPigTrack currentTrack = doctorPigTrackDao.findByPigId(modifyEvent.getPigId());
        DoctorPig oldPig = doctorPigDao.findById(modifyEvent.getPigId());
        Long oldEventId = modifyEvent.getId();
        try {
            //事件能否编辑初步校验
            expectTrue(canModify(modifyEvent), "event.not.allow.modify");

            //1.处理猪事件编辑
            modifyPigEventHandle(modifyEvent, doctorEventInfoList, pigOldEventIdList);

            //2.处理关联的猪群事件编辑
            if (TRIGGER_GROUP_EVENT.contains(modifyEvent.getType())) {
                if (!Objects.equals(modifyEvent.getType(), PigEvent.WEAN.getKey()) && isEffectWeanEvent(modifyEvent, oldEventId)) {
                    //是否有断奶事件
                    List<DoctorEventInfo> weanEventInfoList = doctorEventInfoList.stream()
                            .filter(doctorEventInfo -> Objects.equals(doctorEventInfo.getBusinessType(), DoctorEventInfo.Business_Type.PIG.getValue())
                                    && Objects.equals(doctorEventInfo.getEventType(), PigEvent.WEAN.getKey()))
                            .collect(Collectors.toList());
                    if (!weanEventInfoList.isEmpty()) {

                        //获取猪断奶事件
                        DoctorEventInfo weanEventInfo = weanEventInfoList.get(0);
                        DoctorPigEvent weanEvent = doctorPigEventDao.findById(weanEventInfo.getEventId());

                        //获取猪群断奶事件输入
                        BaseGroupInput weanGroupInput = doctorPigEventManager.getHandler(PigEvent.WEAN.getKey()).buildTriggerGroupEventInput(weanEvent);

                        //获取猪群断奶事件前镜像
                        Long toPigEventId = doctorEventRelationDao.findByOriginAndType(weanEventInfo.getEventId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
                        DoctorGroupEvent oldGroupWeanEvent = doctorGroupEventDao.findById(toPigEventId);
                        expectNotNull(oldGroupWeanEvent, "find.rel.group.event.failed", weanEventInfo.getEventId());
                        DoctorGroupSnapshot oldGroupWeanSnapshot = doctorGroupSnapshotDao.queryByEventId(oldGroupWeanEvent.getId());
                        expectNotNull(oldGroupWeanSnapshot, "find.per.group.snapshot.failed", oldGroupWeanEvent.getId());
                        DoctorGroupSnapShotInfo oldGroupWeanSnapshotInfo = JSON_MAPPER.fromJson(oldGroupWeanSnapshot.getToInfo(), DoctorGroupSnapShotInfo.class);

                        //构建猪群断奶事件
                        DoctorGroupEvent newGroupWeanEvent = doctorGroupEventManager.buildGroupEvent(new DoctorGroupInputInfo(new DoctorGroupDetail(oldGroupWeanSnapshotInfo.getGroup(), oldGroupWeanSnapshotInfo.getGroupTrack()), weanGroupInput), GroupEventType.WEAN.getValue());
                        expectNotNull(newGroupWeanEvent, "build.group.event.failed");
                        newGroupWeanEvent.setId(oldGroupWeanEvent.getId());

                        //猪群事件编辑
                        doctorEditGroupEventService.elicitDoctorGroupTrack(newGroupWeanEvent);
                    }
                }

                //获取猪群事件输入
                BaseGroupInput newGroupEventInput = doctorPigEventManager.getHandler(modifyEvent.getType()).buildTriggerGroupEventInput(modifyEvent);
                //获取猪群需要修改原事件
                Long toGroupEventId = doctorEventRelationDao.findByOriginAndType(modifyEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
                DoctorGroupEvent oldGroupModifyEvent = doctorGroupEventDao.findById(toGroupEventId);
                expectNotNull(oldGroupModifyEvent, "find.rel.group.event.failed", modifyEvent.getId());
                DoctorGroupSnapshot beforeGroupSnapshot = doctorGroupSnapshotDao.queryByEventId(oldGroupModifyEvent.getId());
                expectNotNull(beforeGroupSnapshot, "find.per.group.snapshot.failed", oldGroupModifyEvent.getId());
                DoctorGroupSnapShotInfo beforeGroupSnapShotInfo = JSON_MAPPER.fromJson(beforeGroupSnapshot.getToInfo(), DoctorGroupSnapShotInfo.class);
                DoctorGroupEvent modifyGroupEvent = doctorGroupEventManager.buildGroupEvent(new DoctorGroupInputInfo(new DoctorGroupDetail(beforeGroupSnapShotInfo.getGroup(), beforeGroupSnapShotInfo.getGroupTrack()), newGroupEventInput), oldGroupModifyEvent.getType());
                expectNotNull(modifyGroupEvent, "build.group.event.failed");
                modifyGroupEvent.setId(oldGroupModifyEvent.getId());

                //猪群事件编辑
                doctorEditGroupEventService.elicitDoctorGroupTrack(modifyGroupEvent);

            }
        } catch (Exception e) {
            //Map<Integer, List<DoctorEventInfo>> businessTypeMap = doctorEventInfoList.stream().collect(Collectors.groupingBy(DoctorEventInfo::getBusinessType));

            //回滚猪事件编辑
            doctorPigEventManager.modifyPidEventRollback(doctorEventInfoList, pigOldEventIdList, currentTrack, oldPig);

            log.info("modify pig event handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            throw e;
        }
        doctorPigEventDao.updateEventsStatus(pigOldEventIdList, EventStatus.INVALID.getValue());
        log.info("modifyPigEventHandleImpl ending");
    }

    /**
     * 事件能否编辑初步校验
     * @param modifyEvent 编辑事件
     * @return 能否编辑
     */
    private Boolean canModify(DoctorPigEvent modifyEvent) {
        return Objects.equals(modifyEvent.getKind(), DoctorEventModifyRequest.TYPE.PIG.getValue())
                && !NOT_MODIFY_EVENT.contains(modifyEvent.getType())
                && Objects.equals(modifyEvent.getStatus(), EventStatus.VALID.getValue())
                && Objects.equals(modifyEvent.getIsAuto(), IsOrNot.NO.getValue());
    }

    /**
     * 猪事件编辑具体实现
     * @param modifyEvent 编辑之后的事件
     * @param doctorEventInfoList 事件信息
     * @param oldEventIdList 原事件id列表
     */
    private void modifyPigEventHandle (DoctorPigEvent modifyEvent, List<DoctorEventInfo> doctorEventInfoList, List<Long> oldEventIdList) {
        modifyEvent.setIsModify(IsOrNot.YES.getValue());
        modifyEvent.setStatus(EventStatus.VALID.getValue());
        //原事件id
        Long oldEventId = modifyEvent.getId();
        oldEventIdList.add(oldEventId);

        //获取后续事件
        List<DoctorPigEvent> followEventList = doctorPigEventDao.findFollowEvents(modifyEvent.getPigId(), oldEventId)
                .stream().filter(doctorPigEvent -> !IGNORE_EVENT.contains(doctorPigEvent.getType())).collect(Collectors.toList());
        //将原事件状态置为无效
        oldEventIdList.addAll(followEventList.stream().map(DoctorPigEvent::getId).collect(Collectors.toList()));
        doctorPigEventDao.updateEventsStatus(oldEventIdList, EventStatus.HANDLING.getValue());

        //获取修改前猪track
        DoctorPigSnapshot lastPigSnapshot;
        if (!Objects.equals(modifyEvent.getType(), PigEvent.ENTRY.getKey())) {
            lastPigSnapshot = doctorPigSnapshotDao.queryByEventId(oldEventId);
        } else {
            lastPigSnapshot = doctorPigSnapshotDao.findByToEventId(oldEventId);
        }
        expectNotNull(lastPigSnapshot, "find.per.pig.snapshot.failed", oldEventId);
        DoctorPigTrack fromTrack = JSON_MAPPER.fromJson(lastPigSnapshot.getToPigInfo(), DoctorPigSnapShotInfo.class).getPigTrack();

        //获取事件处理器
        DoctorPigEventHandler handler = doctorPigEventManager.getHandler(modifyEvent.getType());
        //事件校验
        handler.handleCheck(modifyEvent, fromTrack);
        //事物处理事件
        //handler.handle(doctorEventInfoList, modifyEvent, fromTrack);
        doctorPigEventManager.transactionalHandle(handler, doctorEventInfoList, modifyEvent, fromTrack);

        if (followEventList.isEmpty()) {
            return;
        }
        //处理后续事件
        followEventList.forEach(followEvent -> followPigEventHandle(doctorEventInfoList, followEvent));
    }

    /**
     * 处理后续猪事件
     * @param doctorEventInfoList 事件信息列表
     * @param executeEvent 后续事件
     */
    private void followPigEventHandle(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent) {
        log.info("followPigEventHandle stating, executeEvent:{}", executeEvent);
        //获取事件执行前track
        DoctorPigTrack fromTrack = doctorPigTrackDao.findByPigId(executeEvent.getPigId());

        //设置事件属性
        executeEvent.setIsModify(IsOrNot.YES.getValue());
        executeEvent.setStatus(EventStatus.VALID.getValue());
        //如果是断奶事件则要重新设置断奶数量
        if (Objects.equals(executeEvent.getType(), PigEvent.WEAN.getKey())) {
            DoctorWeanDto weanDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorWeanDto.class);
            weanDto.setWeanPigletsCount(fromTrack.getUnweanQty());
            executeEvent.setExtra(JSON_MAPPER.toJson(weanDto));
            executeEvent.setWeanCount(fromTrack.getUnweanQty());
            executeEvent.setDesc(weanDto.getEventDesc());
        }

        //获取事件处理器
        DoctorPigEventHandler handler = doctorPigEventManager.getHandler(executeEvent.getType());
        //事件校验
        handler.handleCheck(executeEvent, fromTrack);
        //事件处理
        //handler.handle(doctorEventInfoList, executeEvent, fromTrack);
        doctorPigEventManager.transactionalHandle(handler, doctorEventInfoList, executeEvent, fromTrack);
    }

    /**
     * 是否会影响断奶事件
     * @param modifyEvent 编辑后的事件
     * @param oldEventId 原事件id
     * @return 是否影响
     */
    private boolean isEffectWeanEvent(DoctorPigEvent modifyEvent, Long oldEventId) {
        DoctorPigEvent oldPigEvent = doctorPigEventDao.findById(oldEventId);
        //
        if (Objects.equals(oldPigEvent.getType(), PigEvent.FARROWING.getKey())
                && !Objects.equals(oldPigEvent.getLiveCount(), modifyEvent.getLiveCount())) {
            return true;
        }

        if (Objects.equals(oldPigEvent.getType(), PigEvent.PIGLETS_CHG.getKey())) {
            DoctorPigletsChgDto oldPigletsChgDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorPigletsChgDto.class);
            DoctorPigletsChgDto newPigletsChgDto = JSON_MAPPER.fromJson(modifyEvent.getExtra(), DoctorPigletsChgDto.class);
            return !Objects.equals(oldPigletsChgDto.getPigletsCount(), newPigletsChgDto.getPigletsCount());
        }
        return false;
    }
}
