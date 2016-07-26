package io.terminus.doctor.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.constants.DoctorPigSnapshotConstants;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: workflow 事件处理方式
 */
@Slf4j
public abstract class DoctorAbstractEventHandler implements DoctorEventCreateHandler{

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    protected final DoctorPigDao doctorPigDao;

    protected final DoctorPigEventDao doctorPigEventDao;

    protected final DoctorPigTrackDao doctorPigTrackDao;

    protected final DoctorPigSnapshotDao doctorPigSnapshotDao;

    protected final DoctorRevertLogDao doctorRevertLogDao;

    @Autowired
    public DoctorAbstractEventHandler(DoctorPigDao doctorPigDao,
                                      DoctorPigEventDao doctorPigEventDao,
                                      DoctorPigTrackDao doctorPigTrackDao,
                                      DoctorPigSnapshotDao doctorPigSnapshotDao,
                                      DoctorRevertLogDao doctorRevertLogDao){
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorRevertLogDao = doctorRevertLogDao;
    }

    @Override
    public Boolean preHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        // 默认可执行
        return Boolean.TRUE;
    }

    @Override
    public void handler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        // create event info
        DoctorPigEvent doctorPigEvent = buildAllPigDoctorEvent(basic, extra);
        doctorPigEventDao.create(doctorPigEvent);
        context.put("doctorPigEventId", doctorPigEvent.getId());

        // update track info
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());
        String currentPigTrackSnapShot = JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack);
        DoctorPigTrack refreshPigTrack = updateDoctorPigTrackInfo(doctorPigTrack, basic, extra, context);
        doctorPigTrackDao.update(refreshPigTrack);

        // create snapshot info
        // snapshot create
        DoctorPigSnapshot doctorPigSnapshot = DoctorPigSnapshot.builder()
                .pigId(doctorPigEvent.getPigId()).farmId(doctorPigEvent.getFarmId()).orgId(doctorPigEvent.getOrgId()).eventId(doctorPigEvent.getId())
                .build();
        doctorPigSnapshot.setPigInfoMap(ImmutableMap.of(DoctorPigSnapshotConstants.PIG_TRACK, currentPigTrackSnapShot));
        doctorPigSnapshotDao.create(doctorPigSnapshot);

        afterEventCreateHandle(doctorPigEvent, refreshPigTrack, doctorPigSnapshot, extra);

        // 当前事件影响的Id 方式
        context.put("createEventResult",
                JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
                        ImmutableMap.of("doctorPigId", basic.getPigId(),
                                "doctorEventId", doctorPigEvent.getId(), "doctorSnapshotId", doctorPigSnapshot.getId())));
    }

    @Override
    public void afterHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {

    }

    /**
     * 事件对母猪的状态信息的影响
     * @param doctorPigTrack 基础的母猪事件信息
     * @param basic 录入基础信息内容
     * @param extra 事件关联的信息内容
     * @param content  执行上下文信息
     * @return
     */
    protected abstract DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack,
                                                            DoctorBasicInputInfoDto basic,
                                                            Map<String,Object> extra, Map<String, Object> content);

    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack,
                                          DoctorPigSnapshot doctorPigSnapshot, Map<String,Object> extra){

    }

    private DoctorPigEvent buildAllPigDoctorEvent(DoctorBasicInputInfoDto basic, Map<String,Object> extra){
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigId(basic.getPigId()).pigCode(basic.getPigCode())
                .eventAt(DateTime.now().toDate()).type(basic.getEventType())
                .kind(basic.getPigType()).name(basic.getEventName()).desc(basic.getEventDesc()).relEventId(basic.getRelEventId())
                .barnId(basic.getBarnId()).barnName(basic.getBarnName())
                .outId(UUID.randomUUID().toString()) //TODO uuid generate method
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        doctorPigEvent.setExtraMap(extra);
        return doctorPigEvent;
    }
}
