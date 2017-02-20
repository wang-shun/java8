package io.terminus.doctor.event.handler;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn.
 * Date:2017/1/3
 */
@Slf4j
public abstract class DoctorAbstractEventHandler implements DoctorPigEventHandler {

    @Autowired
    protected  DoctorPigDao doctorPigDao;
    @Autowired
    protected  DoctorPigEventDao doctorPigEventDao;
    @Autowired
    protected  DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    protected  DoctorPigSnapshotDao doctorPigSnapshotDao;
    @Autowired
    protected  DoctorRevertLogDao doctorRevertLogDao;
    @Autowired
    protected DoctorBarnDao doctorBarnDao;

    protected static final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_EMPTY_MAPPER;

    @Override
    public void handleCheck(BasePigEventInputDto eventDto, DoctorBasicInputInfoDto basic) {
        checkEventAt(eventDto);
    }

    @Override
    public void handle(List<DoctorEventInfo> doctorEventInfoList, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        //获取镜像有关event和track
        DoctorPigTrack pigSnapshotTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        expectTrue(notNull(pigSnapshotTrack), "pig.track.not.null", inputDto.getPigId());
        DoctorPigEvent pigSnapshotEvent = doctorPigEventDao.queryLastPigEventById(inputDto.getPigId());
        expectTrue(notNull(pigSnapshotEvent), "pig.last.event.not.null", inputDto.getPigId());


        //1.创建事件
        DoctorPigEvent doctorPigEvent = buildPigEvent(basic, inputDto);
        doctorPigEventDao.create(doctorPigEvent);

        //2.创建镜像
        DoctorPigSnapshot doctorPigSnapshot = createPigSnapshot(pigSnapshotTrack, pigSnapshotEvent,  doctorPigEvent.getId());
        doctorPigSnapshotDao.create(doctorPigSnapshot);

        //3.创建或更新track
        DoctorPigTrack doctorPigTrack = createOrUpdatePigTrack(basic, inputDto);
        doctorPigTrackDao.update(doctorPigTrack);

        //4.特殊处理
        specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);

        //5.记录发生的事件信息
        DoctorBarn doctorBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
        DoctorEventInfo doctorEventInfo = DoctorEventInfo.builder()
                .orgId(doctorPigEvent.getOrgId())
                .farmId(doctorPigEvent.getFarmId())
                .eventId(doctorPigEvent.getId())
                .eventAt(doctorPigEvent.getEventAt())
                .kind(doctorPigEvent.getKind())
                .mateType(doctorPigEvent.getDoctorMateType())
                .pregCheckResult(doctorPigEvent.getPregCheckResult())
                .businessId(doctorPigEvent.getPigId())
                .code(doctorPigEvent.getPigCode())
                .status(doctorPigTrack.getStatus())
                .businessType(DoctorEventInfo.Business_Type.PIG.getValue())
                .eventType(doctorPigEvent.getType())
                .pigType(doctorBarn.getPigType())
                .build();
        doctorEventInfoList.add(doctorEventInfo);

        //6.触发事件
        triggerEvent(doctorEventInfoList, doctorPigEvent, doctorPigTrack, inputDto, basic);
    }

    /**
     * 构建基础事件信息
     * @param basic
     * @param inputDto
     * @return
     */
    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigId(inputDto.getPigId()).pigCode(inputDto.getPigCode())
                .eventAt(generateEventAt(inputDto.eventAt())).type(inputDto.getEventType())
                .barnId(inputDto.getBarnId()).barnName(inputDto.getBarnName())
                .kind(inputDto.getPigType()).relPigEventId(inputDto.getRelPigEventId()).relGroupEventId(inputDto.getRelGroupEventId())
                .name(inputDto.getEventName()).desc(basic.generateEventDescFromExtra(inputDto))//.relEventId(basic.getRelEventId())
                .operatorId(MoreObjects.firstNonNull(inputDto.getOperatorId(), basic.getStaffId()))
                .operatorName(StringUtils.hasText(inputDto.getOperatorName()) ? inputDto.getOperatorName() : basic.getStaffName())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .isAuto(MoreObjects.firstNonNull(inputDto.getIsAuto(), IsOrNot.NO.getValue()))
                .npd(0)
                .dpnpd(0)
                .pfnpd(0)
                .plnpd(0)
                .psnpd(0)
                .pynpd(0)
                .ptnpd(0)
                .jpnpd(0)
                .build();
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        if (doctorPigTrack != null) {
            doctorPigEvent.setPigStatusBefore(doctorPigTrack.getStatus());
            doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
        }
        doctorPigEvent.setExtraMap(inputDto.toMap());
        return doctorPigEvent;
    }

    //创建猪跟踪和镜像表
    protected DoctorPigSnapshot createPigSnapshot(DoctorPigTrack doctorPigTrack, DoctorPigEvent doctorPigEvent, Long currentEventId) {
        DoctorPig snapshotPig = doctorPigDao.findById(doctorPigEvent.getPigId());
        expectTrue(notNull(snapshotPig), "pig.not.null", doctorPigEvent.getPigId());


        //创建猪镜像
        return DoctorPigSnapshot.builder()
                .pigId(snapshotPig.getId())
                .farmId(snapshotPig.getFarmId())
                .orgId(snapshotPig.getOrgId())
                .eventId(currentEventId)
                .pigInfo(JSON_MAPPER.toJson(
                        DoctorPigSnapShotInfo.builder().pig(snapshotPig).pigTrack(doctorPigTrack).pigEvent(doctorPigEvent).build()))
                .build();
    }

    /**
     * 构建需要更新的track信息
     * @param basic
     * @param inputDto
     * @return
     */
    protected abstract DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto);

    /**
     * 事件的创建后的补充和特殊处理
     * @param doctorPigEvent
     * @param doctorPigTrack
     * @param inputDto
     * @param basic
     */
    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){
        doctorPigEvent.setPigStatusAfter(doctorPigTrack.getStatus());
        doctorPigEventDao.update(doctorPigEvent);
    }

    /**
     * 触发事件, 触发其他事件时需要实现此方法
     * @param doctorEventInfoList
     * @param doctorPigEvent
     * @param doctorPigTrack
     * @param inputDto 事件输入信息
     * @param basic 基础信息
     */
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){

    }

    /**
     * 构建自动事件的共有信息(原事件与触发事件为同一头猪时)
     * @param fromInputDto 原事件信息
     * @param toInputDto 被触发事件信息
     * @param basic 基础信息
     * @param pigEvent 被触发的事件
     */
    protected void buildAutoEventCommonInfo(BasePigEventInputDto fromInputDto, BasePigEventInputDto toInputDto, DoctorBasicInputInfoDto basic, PigEvent pigEvent, Long fromEventId) {
        toInputDto.setIsAuto(IsOrNot.YES.getValue());
        toInputDto.setPigId(fromInputDto.getPigId());
        toInputDto.setPigCode(fromInputDto.getPigCode());
        toInputDto.setPigType(fromInputDto.getPigType());
        toInputDto.setBarnId(fromInputDto.getBarnId());
        toInputDto.setBarnName(fromInputDto.getBarnName());
        toInputDto.setRelPigEventId(fromEventId);
        toInputDto.setEventName(pigEvent.getName());
        toInputDto.setEventType(pigEvent.getKey());
        toInputDto.setEventDesc(pigEvent.getDesc());
    }

    protected Date generateEventAt(Date eventAt){
        if(eventAt != null){
            Date now = new Date();
            if(DateUtil.inSameDate(eventAt, now)){
                // 如果处在今天, 则使用此刻瞬间
                return now;
            } else {
                // 如果不在今天, 则将时间置为0, 只保留日期
                return Dates.startOfDay(eventAt);
            }
        }
        return null;
    }

    /**
     * 事件日期校验
     * @param inputDto
     */
    private void checkEventAt(BasePigEventInputDto inputDto) {
        if (Objects.equals(inputDto.getPigType(), PigEvent.ENTRY.getKey())) {
            return;
        }
        Date eventAt = inputDto.eventAt();
        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventById(inputDto.getPigId());
        if (lastEvent != null && Dates.startOfDay(eventAt).before(Dates.startOfDay(lastEvent.getEventAt()))) {
            throw new InvalidException("event.at.range.error", DateUtil.toDateString(lastEvent.getEventAt()), DateUtil.toDateString(eventAt), inputDto.getPigCode());
        }
    }
}
