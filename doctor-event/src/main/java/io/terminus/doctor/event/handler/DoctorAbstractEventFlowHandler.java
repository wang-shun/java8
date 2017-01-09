package io.terminus.doctor.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
    protected final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorAbstractEventFlowHandler(DoctorPigDao doctorPigDao,
                                          DoctorPigEventDao doctorPigEventDao,
                                          DoctorPigTrackDao doctorPigTrackDao,
                                          DoctorPigSnapshotDao doctorPigSnapshotDao,
                                          DoctorRevertLogDao doctorRevertLogDao,
                                          DoctorBarnDao doctorBarnDao) {
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorRevertLogDao = doctorRevertLogDao;
        this.doctorBarnDao = doctorBarnDao;
    }

//    @Override
//    public void handle(Execution execution) {
//        try {
//            // get data
//            Map<String, String> flowDataMap = OBJECT_MAPPER.readValue(execution.getFlowData(), JacksonType.MAP_OF_STRING);
//            DoctorBasicInputInfoDto doctorBasicInputInfoDto = OBJECT_MAPPER.readValue(flowDataMap.get("basic"), DoctorBasicInputInfoDto.class);
//            Map<String, Object> extraInfo = OBJECT_MAPPER.readValue(flowDataMap.get("extra"), JacksonType.MAP_OF_OBJECT);
//            Map<String, Object> context = Maps.newHashMap();
//
//            DoctorPigTrack snapshotTrack = doctorPigTrackDao.findByPigId(doctorBasicInputInfoDto.getPigId());
//            DoctorPig snapshotPig = doctorPigDao.findById(doctorBasicInputInfoDto.getPigId());
//
//            // bean date
//            DoctorPigEvent doctorPigEvent = buildAllPigDoctorEvent(doctorBasicInputInfoDto, extraInfo);
//            DoctorPigTrack doctorPigTrack = BeanMapper.map(snapshotTrack, DoctorPigTrack.class);
//
//            DoctorPigSnapshot snapshot = DoctorPigSnapshot.builder()
//                    .pigId(snapshotPig.getId())
//                    .farmId(snapshotPig.getFarmId())
//                    .orgId(snapshotPig.getOrgId())
//                    .build();
//
//            //哺乳母猪所带仔猪的猪群id
//            Long farrowGroupId = doctorPigTrack.getGroupId();
//            Long farrowBarId = doctorPigTrack.getCurrentBarnId();
//
//            /** 重要！在事件上标记母猪的猪群id **/
//            doctorPigEvent.setGroupId(farrowGroupId);
//
//            // 当前 猪 状态 对录入数据影响
//            IsOrNot isRePregCheckPositive = eventCreatePrepare(execution, doctorPigEvent, doctorPigTrack, doctorBasicInputInfoDto, extraInfo, context);
//
//            //如果是重复妊娠检查的逆向事件, 需要更新旧的空怀事件
//            if (Objects.equals(isRePregCheckPositive, IsOrNot.NO)) {
//                doctorPigEventDao.create(doctorPigEvent);
//            } else {
//                doctorPigEventDao.update(doctorPigEvent);
//            }
//
//            //创建猪镜像
//            snapshot.setEventId(doctorPigEvent.getId());
//            snapshot.setPigInfo(JsonMapper.nonEmptyMapper().toJson(
//                    DoctorPigSnapShotInfo.builder().pig(snapshotPig).pigTrack(snapshotTrack).pigEvent(doctorPigEvent).build()));
//            doctorPigSnapshotDao.create(snapshot);
//
//            context.put("doctorPigEventId", doctorPigEvent.getId());
//
//            // update track info
//            DoctorPigTrack refreshPigTrack = updateDoctorPigTrackInfo(execution, doctorPigTrack, doctorBasicInputInfoDto, extraInfo, context);
//            doctorPigTrackDao.update(refreshPigTrack);
//
//            eventCreatedAfter(execution, doctorPigEvent, doctorPigTrack, doctorBasicInputInfoDto, extraInfo, context);
//
//            // 特殊 事件信息处理
//            specialFlowHandler(execution, doctorBasicInputInfoDto, extraInfo, context);
//
//            afterEventCreateHandle(doctorPigEvent, doctorPigTrack, farrowGroupId, farrowBarId);
//
//            // 当前事件影响的Id 方式
//            flowDataMap.put("createEventResult",
//                    JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
//                            ImmutableMap.of("doctorPigId", doctorBasicInputInfoDto.getPigId(),
//                                    "doctorEventId", doctorPigEvent.getId(), "doctorSnapshotId", snapshot.getId())));
//            flowDataMap.put("event", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigEvent));
//            flowDataMap.put("track", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack));
//            execution.setFlowData(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(flowDataMap));
//        } catch (IllegalStateException | ServiceException e) {
//            throw e;
//        } catch (Exception e) {
//            DoctorAbstractEventFlowHandler.log.error("handle execute fail, cause:{}", Throwables.getStackTraceAsString(e));
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//
//    /**
//     * event 事件 回掉类型接口
//     *
//     * @param execution
//     * @param basicInputInfoDto
//     * @param extra
//     * @param context
//     */
//    private IsOrNot eventCreatePrepare(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
//                                    Map<String, Object> extra, Map<String, Object> context) {
//        // create event info
//        //添加当前事件发生前猪的状态
//        doctorPigEvent.setPigStatusBefore(doctorPigTrack.getStatus());
//
//        return  eventCreatePreHandler(execution, doctorPigEvent, doctorPigTrack, basicInputInfoDto, extra, context);
//    }
//
//    /**
//     * 用于子类的覆盖
//     *
//     * @param execution
//     * @param doctorPigEvent
//     * @param doctorPigTrack
//     * @param basicInputInfoDto
//     * @param extra
//     * @param context
//     * @return 是否是重复妊娠检查事件逆向事件(如果是, 要更新原来的空怀事件)
//     */
//    protected IsOrNot eventCreatePreHandler(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
//                                            Map<String, Object> extra, Map<String, Object> context) {
//        return IsOrNot.NO;
//    }
//
//
//    /**
//     * event 事件创建后用于二次更新
//     *
//     * @param execution
//     * @param basicInputInfoDto
//     * @param extra
//     * @param context
//     */
//    private void eventCreatedAfter(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
//                                   Map<String, Object> extra, Map<String, Object> context) {
//
//
//        eventCreateAfterHandler(execution, doctorPigEvent, doctorPigTrack, basicInputInfoDto, extra, context);
//
//        Map<String, Object> extraMap = JsonMapper.nonEmptyMapper().fromJson(doctorPigTrack.getExtra(),
//                JsonMapper.nonEmptyMapper().createCollectionType(Map.class, String.class, Object.class));
//
//        //往事件中添加公猪code
//        doctorPigEvent.setBoarCode(Objects.isNull(extraMap.get("matingBoarPigCode")) ? null : Objects.toString(extraMap.get("matingBoarPigCode")));
//        //往事件当中添加事件发生之后猪的状态
//        doctorPigEvent.setPigStatusAfter(doctorPigTrack.getStatus());
//        //添加时间发生之后母猪的胎次
//        doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
//        doctorPigEventDao.update(doctorPigEvent);
//
//        //往pigTrack 当中添加猪舍类型
//        if (notNull(doctorPigTrack.getCurrentBarnId())) {
//            DoctorBarn doctorBarn = getBarnById(doctorPigTrack.getCurrentBarnId());
//            if (notNull(doctorBarn)) {
//                doctorPigTrack.setCurrentBarnType(doctorBarn.getPigType());
//            }
//        }
//    }
//
//    //根据id查猪舍
//    protected DoctorBarn getBarnById(Long barnId) {
//        return doctorBarnDao.findById(barnId);
//    }
//
//    /**
//     * event 事件创建后用于二次更新 用于子类的覆盖
//     *
//     * @param execution
//     * @param doctorPigEvent
//     * @param doctorPigTrack
//     * @param basicInputInfoDto
//     * @param extra
//     * @param context
//     */
//    protected void eventCreateAfterHandler(Execution execution, DoctorPigEvent doctorPigEvent, final DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto,
//                                           Map<String, Object> extra, Map<String, Object> context) {
//
//    }
//
//    protected void afterEventCreateHandle(DoctorPigEvent pigEvent, DoctorPigTrack pigTrack, Long farrowGroupId, Long farrowBarnId) {
//
//    }
//
//    /**
//     * 事件对母猪的状态信息的影响
//     *
//     * @param doctorPigTrack 基础的母猪事件信息
//     * @param basic          录入基础信息内容
//     * @param extra          事件关联的信息内容
//     * @param context        执行上下文环境
//     * @return
//     */
//    protected abstract DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack,
//                                                               DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context);
//
//    /**
//     * 不同子类调用方式
//     *
//     * @param execution
//     * @param basic
//     * @param extra
//     * @param context
//     */
//    protected void specialFlowHandler(Execution execution, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
//
//    }
//
//    protected DoctorPigEvent buildAllPigDoctorEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
//        AbstractPigEventInputDto abstractPigEventInputDto = DoctorBasicInputInfoDto.transFromPigEventAndExtra(PigEvent.from(basic.getEventType()), extra);
//        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
//                .orgId(basic.getOrgId()).orgName(basic.getOrgName())
//                .farmId(basic.getFarmId()).farmName(basic.getFarmName())
//                .pigId(basic.getPigId()).pigCode(basic.getPigCode())
//                .eventAt(basic.generateEventAtFromExtra(extra)).type(basic.getEventType())
//                .kind(basic.getPigType()).name(basic.getEventName()).desc(basic.generateEventDescFromExtra(extra)).relEventId(basic.getRelEventId())
//                .barnId(basic.getBarnId()).barnName(basic.getBarnName())
//                .operatorId(abstractPigEventInputDto.getOperatorId()).operatorName(abstractPigEventInputDto.getOperatorName())
//                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName()).isAuto(MoreObjects.firstNonNull(basic.getIsAuto(), IsOrNot.NO.getValue()))
//                .relPigEventId(basic.getRelPigEventId())
//                .npd(0)
//                .dpnpd(0)
//                .pfnpd(0)
//                .plnpd(0)
//                .psnpd(0)
//                .pynpd(0)
//                .ptnpd(0)
//                .jpnpd(0)
//                .build();
//        doctorPigEvent.setExtraMap(extra);
//        //查询上次的事件
//        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventById(basic.getPigId());
//        if (notNull(lastEvent)) {
//            doctorPigEvent.setRelEventId(lastEvent.getId());
//        }
//        return doctorPigEvent;
//    }
}
