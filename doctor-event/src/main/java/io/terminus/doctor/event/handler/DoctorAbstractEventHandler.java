package io.terminus.doctor.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: workflow 事件处理方式
 */
@Slf4j
public abstract class DoctorAbstractEventHandler implements DoctorEventCreateHandler {

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
                                      DoctorRevertLogDao doctorRevertLogDao) {
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
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());

        eventCreatePrepare(doctorPigEvent, doctorPigTrack, basic, extra, context);
        doctorPigEventDao.create(doctorPigEvent);
        context.put("doctorPigEventId", doctorPigEvent.getId());

        // create track snapshot
        createPigTrackSnapshot(doctorPigEvent, basic, extra, context);
    }

    @Override
    public void afterHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {

    }


    /**
     * event 事件 回掉类型接口
     *
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    private void eventCreatePrepare(DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                    Map<String, Object> extra, Map<String, Object> context) {
        //添加当前事件发生前猪的状态
        doctorPigEvent.setPigStatusBefore(doctorPigTrack.getStatus());


        eventCreatePreHandler(doctorPigEvent, doctorPigTrack, basicInputInfoDto, extra, context);
    }

    /**
     * 用于子类的覆盖
     *
     * @param doctorPigEvent
     * @param doctorPigTrack
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    protected void eventCreatePreHandler(DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                         Map<String, Object> extra, Map<String, Object> context) {

    }

    /**
     * event 事件创建后用于二次更新
     *
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    private void eventCreatedAfter(DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                   Map<String, Object> extra, Map<String, Object> context) {


        eventCreateAfterHandler(doctorPigEvent, doctorPigTrack, basicInputInfoDto, extra, context);

        //往事件当中添加事件发生之后猪的状态
        doctorPigEvent.setPigStatusAfter(doctorPigTrack.getStatus());
        //添加时间发生之后母猪的胎次
        doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
        doctorPigEventDao.update(doctorPigEvent);
    }

    /**
     * event 事件创建后用于二次更新 用于子类的覆盖
     *
     * @param doctorPigEvent
     * @param doctorPigTrack
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    protected void eventCreateAfterHandler(DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                           Map<String, Object> extra, Map<String, Object> context) {

    }

    /**
     * 事件对母猪的状态信息的影响
     *
     * @param doctorPigTrack 基础的母猪事件信息
     * @param basic          录入基础信息内容
     * @param extra          事件关联的信息内容
     * @param content        执行上下文信息
     * @return
     */
    protected abstract DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack,
                                                               DoctorBasicInputInfoDto basic,
                                                               Map<String, Object> extra, Map<String, Object> content);

    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack,
                                          DoctorPigSnapshot doctorPigSnapshot, Map<String, Object> extra) {

    }

    protected DoctorPigEvent buildAllPigDoctorEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigId(basic.getPigId()).pigCode(basic.getPigCode())
                .eventAt(DateTime.now().toDate()).type(basic.getEventType())
                .kind(basic.getPigType()).name(basic.getEventName()).desc(basic.getEventDesc()).relEventId(basic.getRelEventId())
                .barnId(basic.getBarnId()).barnName(basic.getBarnName())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        doctorPigEvent.setExtraMap(extra);
        //查询上次的事件
        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventInWorkflow(basic.getPigId(), null);
        if (notNull(lastEvent)) {
            doctorPigEvent.setRelEventId(lastEvent.getId());
        }
        return doctorPigEvent;
    }

    //创建猪跟踪和镜像表
    protected void createPigTrackSnapshot(DoctorPigEvent doctorPigEvent, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        // update track info
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());
        String currentPigTrackSnapShot = JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack);
        DoctorPigTrack refreshPigTrack = updateDoctorPigTrackInfo(doctorPigTrack, basic, extra, context);
        doctorPigTrackDao.update(refreshPigTrack);
        //二次更新event
        eventCreatedAfter(doctorPigEvent, refreshPigTrack, basic, extra, context);
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
}
