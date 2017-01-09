package io.terminus.doctor.event.handler;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorPublishEventDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * Created by xjn.
 * Date:2017-1-3
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

    @Autowired(required = false)
    protected Publisher publisher;

    protected static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Override
    public void preHandle(BasePigEventInputDto eventDto, DoctorBasicInputInfoDto basic) {

    }

    @Override
    public void handle(List<DoctorEventInfo> doctorEventInfoList, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorPigEvent doctorPigEvent = buildPigEvent(basic, inputDto);
        doctorPigEventDao.create(doctorPigEvent);

        DoctorPigTrack doctorPigTrack = createOrUpdatePigTrack(basic, inputDto);
        if (Objects.equals(basic.getEventType(), PigEvent.ENTRY.getKey())) {
            doctorPigTrackDao.create(doctorPigTrack);
        } else {
            doctorPigTrackDao.update(doctorPigTrack);
        }

        DoctorPigSnapshot doctorPigSnapshot = createPigSnapshot(doctorPigTrack, doctorPigEvent);
        doctorPigSnapshotDao.create(doctorPigSnapshot);

        specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);

        DoctorEventInfo doctorEventInfo = DoctorEventInfo.builder()
                .orgId(doctorPigEvent.getOrgId())
                .farmId(doctorPigEvent.getFarmId())
                .eventId(doctorPigEvent.getId())
                .eventAt(doctorPigEvent.getEventAt())
                .businessId(doctorPigEvent.getPigId())
                .code(doctorPigEvent.getPigCode())
                .status(doctorPigTrack.getStatus())
                .businessType(DoctorEventInfo.Business_Type.PIG.getValue())
                .eventType(basic.getEventType())
                .build();
        doctorEventInfoList.add(doctorEventInfo);
        triggerEvent(doctorEventInfoList, doctorPigEvent, doctorPigTrack, inputDto, basic);
    }

    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){
        doctorPigEvent.setPigStatusAfter(doctorPigTrack.getStatus());
        doctorPigEventDao.update(doctorPigEvent);
    }

    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){

    }

    @Override
    public List<DoctorPublishEventDto> publishEvent(List<DoctorEventInfo> doctorEventInfoList) {
        return Lists.newArrayList();
    }

    /**
     * 事件对母猪的状态信息的影响
     *
     * @param basic          录入基础信息内容
     * @return
     */
    protected abstract DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto);

    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigId(inputDto.getPigId()).pigCode(inputDto.getPigCode())
                .eventAt(inputDto.eventAt()).type(basic.getEventType())
                .barnId(inputDto.getBarnId()).barnName(inputDto.getBarnName())
                .kind(inputDto.getPigType()).name(basic.getEventName()).desc(basic.generateEventDescFromExtra(inputDto))//.relEventId(basic.getRelEventId())
                .operatorId(inputDto.getOperatorId()).operatorName(inputDto.getOperatorName())
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
        //查询上次的事件
//        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventInWorkflow(basic.getPigId(), null);
//        if (notNull(lastEvent)) {
//            doctorPigEvent.setRelEventId(lastEvent.getId());
//        }
        return doctorPigEvent;
    }

    //创建猪跟踪和镜像表
    protected DoctorPigSnapshot createPigSnapshot(DoctorPigTrack doctorPigTrack, DoctorPigEvent doctorPigEvent) {
        DoctorPig snapshotPig = doctorPigDao.findById(doctorPigEvent.getPigId());

        //创建猪镜像
        DoctorPigSnapshot snapshot = DoctorPigSnapshot.builder()
                .pigId(snapshotPig.getId())
                .farmId(snapshotPig.getFarmId())
                .orgId(snapshotPig.getOrgId())
                .eventId(doctorPigEvent.getId())
                .pigInfo(JsonMapper.nonEmptyMapper().toJson(
                        DoctorPigSnapShotInfo.builder().pig(snapshotPig).pigTrack(doctorPigTrack).pigEvent(doctorPigEvent).build()))
                .build();
        return snapshot;
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
        basic.setEventName(pigEvent.getName());
        basic.setEventType(pigEvent.getKey());
        basic.setEventDesc(pigEvent.getDesc());
    }
}
