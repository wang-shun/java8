package io.terminus.doctor.event.handler;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
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

    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, Map<String, Object> extra) {

    }

    protected DoctorPigEvent buildAllPigDoctorEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        AbstractPigEventInputDto abstractPigEventInputDto = DoctorBasicInputInfoDto.transFromPigEventAndExtra(PigEvent.from(basic.getEventType()), extra);
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigId(basic.getPigId()).pigCode(basic.getPigCode())
                .eventAt(basic.generateEventAtFromExtra(extra)).type(basic.getEventType())
                .kind(basic.getPigType()).name(basic.getEventName()).desc(basic.generateEventDescFromExtra(extra)).relEventId(basic.getRelEventId())
                .barnId(basic.getBarnId()).barnName(basic.getBarnName())
                .operatorId(abstractPigEventInputDto.getOperatorId()).operatorName(abstractPigEventInputDto.getOperatorName())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName()).isAuto(IsOrNot.NO.getValue())
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

    //创建猪跟踪和镜像表
    protected void createPigTrackSnapshot(DoctorPigEvent doctorPigEvent, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        // update track info
        DoctorPig snapshotPig = doctorPigDao.findById(doctorPigEvent.getPigId());
        DoctorPigTrack snapshotTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());
        DoctorPigTrack doctorPigTrack = BeanMapper.map(snapshotTrack, DoctorPigTrack.class);

        DoctorPigTrack refreshPigTrack = updateDoctorPigTrackInfo(doctorPigTrack, basic, extra, context);
        refreshPigTrack.setUpdatorId(basic.getStaffId());
        refreshPigTrack.setUpdatorName(basic.getStaffName());
        doctorPigTrackDao.update(refreshPigTrack);
        //二次更新event
        eventCreatedAfter(doctorPigEvent, refreshPigTrack, basic, extra, context);
        afterEventCreateHandle(doctorPigEvent, refreshPigTrack, extra);

        //创建猪镜像
        DoctorPigSnapshot snapshot = DoctorPigSnapshot.builder()
                .pigId(snapshotPig.getId())
                .farmId(snapshotPig.getFarmId())
                .orgId(snapshotPig.getOrgId())
                .eventId(doctorPigEvent.getId())
                .pigInfo(JsonMapper.nonEmptyMapper().toJson(
                        DoctorPigSnapShotInfo.builder().pig(snapshotPig).pigTrack(snapshotTrack).pigEvent(doctorPigEvent).build()))
                .build();
        doctorPigSnapshotDao.create(snapshot);

        context.put("createEventResult",
                JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
                        ImmutableMap.of("doctorPigId", basic.getPigId(),
                                "doctorEventId", doctorPigEvent.getId(), "doctorSnapshotId", snapshot.getId())));
    }

}
