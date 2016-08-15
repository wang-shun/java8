package io.terminus.doctor.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
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
import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: workflow 事件处理方式
 */
@Slf4j
public abstract class DoctorAbstractEventFlowHandler extends HandlerAware {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    protected final DoctorPigDao doctorPigDao;

    protected final DoctorPigEventDao doctorPigEventDao;

    protected final DoctorPigTrackDao doctorPigTrackDao;

    protected final DoctorPigSnapshotDao doctorPigSnapshotDao;

    protected final DoctorRevertLogDao doctorRevertLogDao;

    @Autowired
    public DoctorAbstractEventFlowHandler(DoctorPigDao doctorPigDao,
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
    public void handle(Execution execution) {
        try {
            // get data
            Map<String, String> flowDataMap = OBJECT_MAPPER.readValue(execution.getFlowData(), JacksonType.MAP_OF_STRING);
            DoctorBasicInputInfoDto doctorBasicInputInfoDto = OBJECT_MAPPER.readValue(flowDataMap.get("basic"), DoctorBasicInputInfoDto.class);
            Map<String, Object> extraInfo = OBJECT_MAPPER.readValue(flowDataMap.get("extra"), JacksonType.MAP_OF_OBJECT);
            Map<String, Object> context = Maps.newHashMap();

            // bean date
            DoctorPigEvent doctorPigEvent = buildAllPigDoctorEvent(doctorBasicInputInfoDto, extraInfo);
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());

            // 当前 猪 状态 对录入数据影响
            eventCreatePrepare(execution, doctorPigEvent, doctorPigTrack, doctorBasicInputInfoDto, extraInfo, context);

            doctorPigEventDao.create(doctorPigEvent);
            context.put("doctorPigEventId", doctorPigEvent.getId());

            // update track info
            String currentPigTrackSnapShot = JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack);
            DoctorPigTrack refreshPigTrack = updateDoctorPigTrackInfo(execution, doctorPigTrack, doctorBasicInputInfoDto, extraInfo, context);
            doctorPigTrackDao.update(refreshPigTrack);


            eventCreatedAfter(execution, doctorPigEvent, doctorPigTrack, doctorBasicInputInfoDto, extraInfo, context);

            // create snapshot info
            // snapshot create
            DoctorPigSnapshot doctorPigSnapshot = DoctorPigSnapshot.builder()
                    .pigId(doctorPigEvent.getPigId()).farmId(doctorPigEvent.getFarmId()).orgId(doctorPigEvent.getOrgId()).eventId(doctorPigEvent.getId())
                    .build();
            doctorPigSnapshot.setPigInfoMap(ImmutableMap.of(DoctorPigSnapshotConstants.PIG_TRACK, currentPigTrackSnapShot));
            doctorPigSnapshotDao.create(doctorPigSnapshot);

            // 特殊 事件信息处理
            specialFlowHandler(execution, doctorBasicInputInfoDto, extraInfo, context);

            afterEventCreateHandle(doctorPigEvent, doctorPigTrack, doctorPigSnapshot, context);

            // 当前事件影响的Id 方式
            flowDataMap.put("createEventResult",
                    JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
                            ImmutableMap.of("doctorPigId", doctorBasicInputInfoDto.getPigId(),
                                    "doctorEventId", doctorPigEvent.getId(), "doctorSnapshotId", doctorPigSnapshot.getId())));
            execution.setFlowData(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(flowDataMap));
        } catch (IllegalStateException e) {
            log.error("handle execute fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw e;
        } catch (Exception e) {
            DoctorAbstractEventFlowHandler.log.error("handle execute fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * event 事件 回掉类型接口
     *
     * @param execution
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    private void eventCreatePrepare(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                    Map<String, Object> extra, Map<String, Object> context) {
        // create event info
        //添加当前事件发生前猪的状态
        doctorPigEvent.setPigStatusBefore(doctorPigTrack.getStatus());

        eventCreatePreHandler(execution, doctorPigEvent, doctorPigTrack, basicInputInfoDto, extra, context);
    }

    /**
     * 用于子类的覆盖
     *
     * @param execution
     * @param doctorPigEvent
     * @param doctorPigTrack
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    protected void eventCreatePreHandler(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                         Map<String, Object> extra, Map<String, Object> context) {

    }


    /**
     * event 事件创建后用于二次更新
     *
     * @param execution
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    private void eventCreatedAfter(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                   Map<String, Object> extra, Map<String, Object> context) {


        eventCreateAfterHandler(execution, doctorPigEvent, doctorPigTrack, basicInputInfoDto, extra, context);

        //往事件当中添加事件发生之后猪的状态
        doctorPigEvent.setPigStatusAfter(doctorPigTrack.getStatus());
        //添加时间发生之后母猪的胎次
        doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
        doctorPigEventDao.update(doctorPigEvent);
    }

    /**
     * event 事件创建后用于二次更新 用于子类的覆盖
     *
     * @param execution
     * @param doctorPigEvent
     * @param doctorPigTrack
     * @param basicInputInfoDto
     * @param extra
     * @param context
     */
    protected void eventCreateAfterHandler(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
                                           Map<String, Object> extra, Map<String, Object> context) {

    }

    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack,
                                          DoctorPigSnapshot doctorPigSnapshot, Map<String, Object> extra) {

    }

    /**
     * 事件对母猪的状态信息的影响
     *
     * @param doctorPigTrack 基础的母猪事件信息
     * @param basic          录入基础信息内容
     * @param extra          事件关联的信息内容
     * @param context        执行上下文环境
     * @return
     */
    protected abstract DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack,
                                                               DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context);

    /**
     * 不同子类调用方式
     *
     * @param execution
     * @param basic
     * @param extra
     * @param context
     */
    protected void specialFlowHandler(Execution execution, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        return;
    }

    private DoctorPigEvent buildAllPigDoctorEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigId(basic.getPigId()).pigCode(basic.getPigCode())
                .eventAt(DateTime.now().toDate()).type(basic.getEventType())
                .kind(basic.getPigType()).name(basic.getEventName()).desc(basic.getEventDesc()).relEventId(basic.getRelEventId())
                .barnId(basic.getBarnId()).barnName(basic.getBarnName())
                .outId(UUID.randomUUID().toString())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .npd(0)
                .dpnpd(0)
                .pfnpd(0)
                .plnpd(0)
                .psnpd(0)
                .pynpd(0)
                .ptnpd(0)
                .jpnpd(0)
                .build();
        doctorPigEvent.setExtraMap(extra);
        //查询上次的事件
        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventInWorkflow(basic.getPigId(), null);
        if (notNull(lastEvent)) {
            doctorPigEvent.setRelEventId(lastEvent.getId());
        }
        return doctorPigEvent;
    }
}
