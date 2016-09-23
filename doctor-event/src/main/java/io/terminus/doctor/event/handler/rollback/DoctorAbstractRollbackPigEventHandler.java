package io.terminus.doctor.event.handler.rollback;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.DoctorRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.model.DoctorRevertLog;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorRevertLogWriteService;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired protected DoctorPigEventReadService doctorPigEventReadService;
    @Autowired protected DoctorPigSnapshotDao doctorPigSnapshotDao;
    @Autowired protected DoctorPigEventDao doctorPigEventDao;
    @Autowired protected DoctorPigTrackDao doctorPigTrackDao;
    @Autowired private DoctorRevertLogWriteService doctorRevertLogWriteService;
    @Autowired private CoreEventDispatcher coreEventDispatcher;
    @Autowired(required = false) private Publisher publisher;

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
        DoctorRevertLog revertLog = handleRollback(pigEvent);
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
    protected abstract DoctorRevertLog handleRollback(DoctorPigEvent pigEvent);

    /**
     * 需要更新的统计
     * @see RollbackType
     */
    protected abstract List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent);

    /**
     * 不涉及状态的事件回滚处理
     * @param pigEvent
     * @param type
     * @return
     */
    protected DoctorRevertLog handleRollbackWithoutStatus(DoctorPigEvent pigEvent, Integer type){
        DoctorPigTrack doctorPigTrack= doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        DoctorPigSnapshot doctorPigSnapshot = doctorPigSnapshotDao.queryLastByPigId(pigEvent.getPigId());
        String fromInfo = MAPPER.toJson(doctorPigTrack) + MAPPER.toJson(doctorPigSnapshot) + MAPPER.toJson(pigEvent);

        doctorPigEventDao.delete(pigEvent.getId());
        return DoctorRevertLog.builder()
                .type(type)
                .fromInfo(fromInfo)
                .toInfo("")
                .build();
    }

    /**
     * 涉及状态的事件回滚处理
     * @param pigEvent
     * @param type
     * @return
     */
    protected DoctorRevertLog handleRollbackWithStatus(DoctorPigEvent pigEvent, Integer type){
        DoctorRevertLog doctorRevertLog = handleRollbackWithoutStatus(pigEvent, type);
        DoctorPigSnapshot doctorPigSnapshot = doctorPigSnapshotDao.queryLastByPigId(pigEvent.getPigId());
        doctorPigSnapshot.setPigInfo(doctorPigSnapshot.getPigInfo());
        if (doctorPigSnapshot != null) {
            doctorPigTrackDao.update((DoctorPigTrack) doctorPigSnapshot.getPigInfoMap().get("doctorPigTrack"));
        }else {
            doctorPigTrackDao.delete(doctorPigTrackDao.findByPigId(pigEvent.getPigId()).getId());
        }
        doctorPigSnapshotDao.delete(doctorPigSnapshot.getId());
        return doctorRevertLog;

    }
}
