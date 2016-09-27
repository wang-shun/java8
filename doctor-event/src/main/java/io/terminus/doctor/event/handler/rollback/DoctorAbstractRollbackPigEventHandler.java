package io.terminus.doctor.event.handler.rollback;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.DoctorRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.model.DoctorRevertLog;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorRevertLogWriteService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 猪事件回滚handler
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
public abstract class DoctorAbstractRollbackPigEventHandler extends DoctorAbstrackRollbackReportHandler implements DoctorRollbackPigEventHandler {

    protected static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Autowired private DoctorRevertLogWriteService doctorRevertLogWriteService;
    @Autowired protected FlowProcessService flowProcessService;
    @Autowired protected DoctorGroupReadService doctorGroupReadService;
    @Autowired protected DoctorGroupDao doctorGroupDao;
    @Autowired protected DoctorGroupEventDao doctorGroupEventDao;
    @Autowired protected DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired protected DoctorPigSnapshotDao doctorPigSnapshotDao;
    @Autowired protected DoctorPigEventDao doctorPigEventDao;
    @Autowired protected DoctorPigTrackDao doctorPigTrackDao;
    @Autowired protected DoctorPigDao doctorPigDao;
    @Autowired protected DoctorPigEventReadService doctorPigEventReadService;
    @Value("${flow.definition.key.sow:sow}")
    protected String sowFlowKey;

    /**
     * 判断能否回滚(1.手动事件 2.三个月内的事件 3.最新事件 4.子类根据事件类型特殊处理)
     */
    @Override
    public final boolean canRollback(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getIsAuto(), IsOrNot.YES.getValue()) &&
                pigEvent.getEventAt().after(DateTime.now().plusMonths(-3).toDate()) &&
                RespHelper.orFalse(doctorPigEventReadService.isLastEvent(pigEvent.getPigId(), pigEvent.getId())) &&
                handleCheck(pigEvent);
    }

    /**
     * 带事务的回滚操作
     */
    @Override
    public final void rollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        handleRollback(pigEvent, operatorId, operatorName);
    }

    /**
     * 更新统计报表(发zk事件)
     */
    @Override
    public final void updateReport(DoctorPigEvent pigEvent) {
        checkAndPublishRollback(handleReport(pigEvent));
    }

    /**
     * 是否是最新事件
     */
    protected boolean isLastEvent(DoctorPigEvent pigEvent) {
        return RespHelper.orFalse(doctorPigEventReadService.isLastEvent(pigEvent.getPigId(), pigEvent.getId()));
    }

    /**
     * 每个子类根据事件类型 判断是否应该由此handler执行回滚
     */
    protected abstract boolean handleCheck(DoctorPigEvent pigEvent);

    /**
     * 处理回滚操作
     */
    @Transactional
    protected abstract void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName);

    /**
     * 需要更新的统计
     * @see RollbackType
     */
    protected abstract List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent);

    /**
     * 不涉及状态的事件回滚处理
     * @param pigEvent 猪事件
     */
    protected void handleRollbackWithoutStatus(DoctorPigEvent pigEvent, Long operatorId, String operatorName){
        DoctorPigSnapshot snapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId());
        DoctorPigSnapShotInfo info = JSON_MAPPER.fromJson(snapshot.getPigInfo(), DoctorPigSnapShotInfo.class);
        doctorPigEventDao.delete(pigEvent.getId());
        doctorPigTrackDao.update(info.getPigTrack());
        doctorPigDao.update(info.getPig());
        doctorPigSnapshotDao.delete(snapshot.getId());
        createDoctorRevertLog(pigEvent, operatorId, operatorName);
    }

    /**
     * 涉及状态的事件回滚处理
     * @param pigEvent 猪事件
     */
    protected void handleRollbackWithStatus(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        handleRollbackWithoutStatus(pigEvent, operatorId, operatorName);
        workFlowRollback(pigEvent);
    }

    /**
     * 回滚工作流
     * @param pigEvent 事件
     */
    protected void workFlowRollback(DoctorPigEvent pigEvent){
        if (Objects.equals(pigEvent.getKind(), DoctorPig.PIG_TYPE.SOW.getKey())){
            flowProcessService.rollBack(sowFlowKey, pigEvent.getPigId());
        }
    }

    /**
     * 创建回滚日志
     * @param pigEvent 事件
     * @return 回滚日志
     */
    protected void createDoctorRevertLog(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        DoctorPigTrack doctorPigTrack= doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        DoctorPig doctorPig= doctorPigDao.findById(pigEvent.getPigId());
        DoctorPigSnapShotInfo fromInfo  = DoctorPigSnapShotInfo.builder()
                .pigEvent(pigEvent)
                .pigTrack(doctorPigTrack)
                .pig(doctorPig)
                .build();

        DoctorPigEvent toPigEvent = doctorPigEventDao.queryLastPigEventById(pigEvent.getPigId());
        DoctorPigTrack toPigTrack = doctorPigTrackDao.findByPigId(toPigEvent.getPigId());
        DoctorPigSnapShotInfo toInfo  = DoctorPigSnapShotInfo.builder()
                .pigEvent(toPigEvent)
                .pigTrack(toPigTrack)
                .pig(doctorPig)
                .build();

        DoctorRevertLog revertLog = new DoctorRevertLog();
        revertLog.setFarmId(pigEvent.getFarmId());
        revertLog.setPigId(pigEvent.getPigId());
        revertLog.setType(pigEvent.getKind());
        revertLog.setFromInfo(JSON_MAPPER.toJson(fromInfo));
        revertLog.setToInfo(JSON_MAPPER.toJson(toInfo));
        revertLog.setReverterId(operatorId);
        revertLog.setReverterName(operatorName);
        RespHelper.orServEx(doctorRevertLogWriteService.createRevertLog(revertLog));
    }

}
