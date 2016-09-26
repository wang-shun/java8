package io.terminus.doctor.event.handler.rollback;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
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

    protected final JsonMapper MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER;
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
    @Override @Transactional
    public final void rollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        DoctorRevertLog revertLog = handleRollback(pigEvent, operatorId, operatorName);
        revertLog.setReverterId(operatorId);
        revertLog.setReverterName(operatorName);
        RespHelper.orServEx(doctorRevertLogWriteService.createRevertLog(revertLog));
    }

    /**
     * 更新统计报表(发zk事件)
     */
    @Override
    public final void updateReport(DoctorPigEvent pigEvent) {
        checkAndPublishRollback(handleReport(pigEvent));
    }

    /**
     * 每个子类根据事件类型 判断是否应该由此handler执行回滚
     */
    protected abstract boolean handleCheck(DoctorPigEvent pigEvent);

    /**
     * 处理回滚操作
     */
    protected abstract DoctorRevertLog handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName);

    /**
     * 需要更新的统计
     * @see RollbackType
     */
    protected abstract List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent);

    /**
     * 不涉及状态的事件回滚处理
     * @param pigEvent 猪事件
     * @param type 拦截的事件类型
     * @return
     */
    protected DoctorRevertLog handleRollbackWithoutStatus(DoctorPigEvent pigEvent, Integer type){
        DoctorPigTrack doctorPigTrack= doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        DoctorPig doctorPig= doctorPigDao.findById(pigEvent.getPigId());
        DoctorPigSnapshot doctorPigSnapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId());
        DoctorPigSnapShotInfo fromInfo  = DoctorPigSnapShotInfo.builder()
                .pigEvent(pigEvent)
                .pigTrack(doctorPigTrack)
                .pig(doctorPig)
                .build();

        DoctorPigSnapShotInfo toInfo  = DoctorPigSnapShotInfo.builder()
                .pigEvent(pigEvent)
                .pigTrack(doctorPigTrack)
                .pig(doctorPig)
                .build();

        doctorPigEventDao.delete(pigEvent.getId());
        doctorPigSnapshotDao.delete(doctorPigSnapshot.getId());
        return DoctorRevertLog.builder()
                .type(type)
                .fromInfo(JSON_MAPPER.toJson(fromInfo))
                .toInfo("")
                .build();
    }

    /**
     * 涉及状态的事件回滚处理
     * @param pigEvent 猪事件
     * @param type 拦截的事件类型
     * @return
     */
    protected DoctorRevertLog handleRollbackWithStatus(DoctorPigEvent pigEvent, Integer type) {
        DoctorPigTrack doctorPigTrack= doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        DoctorPig doctorPig= doctorPigDao.findById(pigEvent.getPigId());
        DoctorPigSnapshot doctorPigSnapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId());
        DoctorPigSnapShotInfo doctorPigSnapShotInfo  = DoctorPigSnapShotInfo.builder()
                .pigEvent(pigEvent)
                .pigTrack(doctorPigTrack)
                .pig(doctorPig)
                .build();
        doctorPigEventDao.delete(pigEvent.getId());
        if (!Objects.equals(pigEvent.getType(), PigEvent.ENTRY.getKey())) {
            doctorPigTrackDao.update(JSON_MAPPER.fromJson(doctorPigSnapshot.getPigInfo(), DoctorPigSnapShotInfo.class).getPigTrack());
        } else {
            doctorPigTrackDao.delete(doctorPigTrackDao.findByPigId(pigEvent.getPigId()).getId());
        }
        doctorPigSnapshotDao.delete(doctorPigSnapshot.getId());
        workFlowRollback(pigEvent);
        return DoctorRevertLog.builder()
                .type(type)
                .fromInfo(JSON_MAPPER.toJson(doctorPigSnapShotInfo))
                .toInfo("")
                .build();
    }

     /* 通用猪事件回滚
     * @param pigEvent 猪事件
     * @return 回滚日志
     */
    protected DoctorRevertLog sampleRollback(DoctorPigEvent pigEvent) {
        DoctorPigSnapshot snapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId());
        return new DoctorRevertLog();
    }

    protected void workFlowRollback(DoctorPigEvent pigEvent){
        if (Objects.equals(pigEvent.getKind(), DoctorPigEvent.kind.Sow.getValue())){
            flowProcessService.rollBack(sowFlowKey, pigEvent.getPigId());
        }
    }

    protected DoctorRevertLog createDoctorRevertLog(DoctorPigEvent pigEvent){
        return null;
    }
}
