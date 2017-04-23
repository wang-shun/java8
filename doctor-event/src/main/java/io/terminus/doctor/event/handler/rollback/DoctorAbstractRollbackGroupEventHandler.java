package io.terminus.doctor.event.handler.rollback;

import com.google.common.collect.Lists;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorEventRelationDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorRevertLog;
import io.terminus.doctor.event.service.DoctorRevertLogWriteService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;

/**
 * Desc: 猪群事件回滚处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
public abstract class DoctorAbstractRollbackGroupEventHandler implements DoctorRollbackGroupEventHandler {

    protected static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Autowired private DoctorRevertLogWriteService doctorRevertLogWriteService;
    @Autowired protected DoctorGroupDao doctorGroupDao;
    @Autowired protected DoctorGroupEventDao doctorGroupEventDao;
    @Autowired protected DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired protected DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    @Autowired protected DoctorPigEventDao doctorPigEventDao;
    @Autowired protected DoctorEventRelationDao doctorEventRelationDao;

    static List<Integer> excludeGroupEvent = Lists.newArrayList(GroupEventType.ANTIEPIDEMIC.getValue(), GroupEventType.DISEASE.getValue());

    /**
     * 判断能否回滚(1.手动事件 2.三个月内的事件 3.最新事件 4.子类根据事件类型特殊处理)
     */
    @Override
    public final boolean canRollback(DoctorGroupEvent groupEvent) {
        return Objects.equals(groupEvent.getIsAuto(), IsOrNot.NO.getValue())
                && Objects.equals(groupEvent.getEventSource(), SourceType.INPUT.getValue())
                && groupEvent.getEventAt().after(DateTime.now().plusMonths(-12).toDate())
                && handleCheck(groupEvent);
    }

    /**
     * 回滚实现
     */
    @Override
    public void rollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        handleRollback(groupEvent, operatorId, operatorName);
    }

    /**
     * 每个子类根据事件类型 判断是否应该由此handler执行回滚
     */
    protected abstract boolean handleCheck(DoctorGroupEvent groupEvent);

    /**
     * 处理回滚操作(事务处理)
     */
    protected abstract void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName);

    /**
     * 是否是最新事件
     */
    protected boolean isLastEvent(DoctorGroupEvent groupEvent) {
        // TODO: 17/3/27 暂时为所有有效事件中最新包括疾病、防疫
        //DoctorGroupEvent lastEvent = doctorGroupEventDao.findLastEventExcludeTypes(groupEvent.getGroupId(), excludeGroupEvent);
        DoctorGroupEvent lastEvent = doctorGroupEventDao.findLastEventByGroupId(groupEvent.getGroupId());
        if (!Objects.equals(groupEvent.getId(), lastEvent.getId())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 单个猪群事件的回滚，可以抽象出来，自取自用
     * @param groupEvent 事件
     */
    protected void sampleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        DoctorGroupSnapshot snapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(groupEvent.getId());

        DoctorGroupSnapshot oldSnapshot = doctorGroupSnapshotDao.queryByEventId(groupEvent.getId());
        expectNotNull(oldSnapshot, "query.pre.snapshot.failed", groupEvent.getId());
        DoctorGroupSnapShotInfo info = JSON_MAPPER.fromJson(oldSnapshot.getToInfo(), DoctorGroupSnapShotInfo.class);

        //删除此事件 -> 回滚猪群跟踪 -> 回滚猪群 -> 删除镜像
        doctorGroupEventDao.delete(groupEvent.getId());
        DoctorGroupTrack newTrack = info.getGroupTrack();
        newTrack.setQuantity(doctorGroupEventDao.getEventCount(newTrack.getGroupId()));
        doctorGroupTrackDao.update(newTrack);
        doctorGroupDao.update(info.getGroup());
        doctorGroupSnapshotDao.delete(snapshot.getId());

        //创建回滚日志
        createRevertLog(groupEvent, snapshot, oldSnapshot, operatorId, operatorName);
    }

    protected void createRevertLog(DoctorGroupEvent groupEvent, DoctorGroupSnapshot snapshot, DoctorGroupSnapshot oldSnapshot, Long operatorId, String operatorName) {
        DoctorRevertLog revertLog = new  DoctorRevertLog();
        revertLog.setFarmId(groupEvent.getFarmId());
        revertLog.setGroupId(groupEvent.getGroupId());
        if (!Objects.equals(groupEvent.getType(), GroupEventType.NEW.getValue())) {
            revertLog.setFromInfo(oldSnapshot.getToInfo());
        }
        revertLog.setToInfo(snapshot.getToInfo());
        revertLog.setType(DoctorRevertLog.Type.GROUP.getValue());
        revertLog.setReverterId(operatorId);
        revertLog.setReverterName(operatorName);
        RespHelper.orServEx(doctorRevertLogWriteService.createRevertLog(revertLog));
    }

    protected static boolean isCloseEvent(DoctorGroupEvent close) {
        return close != null && Objects.equals(close.getType(), GroupEventType.CLOSE.getValue());
    }

    //判断事件链的最后一个事件，是否是最新事件
    protected boolean isRelLastEvent(DoctorGroupEvent event) {
        DoctorGroupEvent tmpEvent = event;
        while (event != null) {
            tmpEvent = event;
            DoctorEventRelation eventRelation = doctorEventRelationDao.findGroupEventByGroupOrigin(event.getId());
            event = isNull(eventRelation) ? null : doctorGroupEventDao.findById(eventRelation.getTriggerGroupEventId());
        }
        return isLastEvent(tmpEvent);
    }

    /**
     * 没有镜像事件处理
     * @param groupEvent 回滚事件
     * @param operatorId 操作人id
     * @param operatorName 操作人姓名
     */
    protected void handleRollbackWithoutSnapshot(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        if (!excludeGroupEvent.contains(groupEvent.getType())) {
            throw new InvalidException("rollback.event.type.error", groupEvent.getId());
        }

        //旧事件需要删除镜像
        DoctorGroupSnapshot groupSnapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(groupEvent.getId());
        if (notNull(groupSnapshot)) {
            sampleRollback(groupEvent, operatorId, operatorName);
            return;
        }

        //新生成的疾病、防疫不需要删除镜像
        doctorGroupEventDao.delete(groupEvent.getId());
    }
}
