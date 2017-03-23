package io.terminus.doctor.event.manager;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.*;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.*;
import io.terminus.doctor.event.handler.group.DoctorGroupEventHandlers;
import io.terminus.doctor.event.handler.usual.DoctorEntryHandler;
import io.terminus.doctor.event.model.*;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Arg;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.acl.Group;
import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:38 17/3/9
 */

@Slf4j
@Component
public class DoctorEditGroupEventManager {

    private DoctorGroupEventHandlers doctorGroupEventHandlers;
    private DoctorGroupEventDao doctorGroupEventDao;
    private DoctorGroupTrackDao doctorGroupTrackDao;
    private DoctorPigEventDao doctorPigEventDao;
    private DoctorEntryHandler doctorEntryHandler;
    private DoctorEventRelationDao doctorEventRelationDao;
    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private DoctorGroupDao doctorGroupDao;
    private static JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    @Autowired
    public DoctorEditGroupEventManager(DoctorGroupEventHandlers doctorGroupEventHandlers,
                                       DoctorGroupEventDao doctorGroupEventDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao,
                                       DoctorPigEventDao doctorPigEventDao,
                                       DoctorEntryHandler doctorEntryHandler,
                                       DoctorEventRelationDao doctorEventRelationDao,
                                       DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                       DoctorGroupDao doctorGroupDao){
        this.doctorGroupEventHandlers = doctorGroupEventHandlers;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorEntryHandler = doctorEntryHandler;
        this.doctorEventRelationDao= doctorEventRelationDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupDao = doctorGroupDao;
    }

    @Transactional
    public Boolean updateDoctorGroupEventStatus(List<Long> doctorGroupEventList, Integer status){
        return doctorGroupEventDao.updateGroupEventStatus(doctorGroupEventList, status);
    }

    @Transactional
    public DoctorGroupTrack elicitDoctorGroupTrack(List<DoctorGroupEvent> triggerDoctorGroupEventList, List<Long> doctorGroupEventList, DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent newEvent){
        DoctorGroupEvent oldEvent = new DoctorGroupEvent();
        BeanMapper.copy(newEvent, oldEvent);
        return doctorGroupEventHandlers.getEventHandlerMap().get(newEvent.getType()).editGroupEvent(triggerDoctorGroupEventList, doctorGroupEventList, doctorGroupTrack, oldEvent, newEvent);
    }


    @Transactional
    public Boolean rollbackElicitEvents(List<DoctorGroupTrack> doctorGroupTrackList, List<Long> newDoctorGroupEvents, List<Long> oldDoctorGroupEvents) {
        Boolean status = doctorGroupEventDao.updateGroupEventStatus(oldDoctorGroupEvents, EventStatus.VALID.getValue());
        status = status && doctorGroupEventDao.updateGroupEventStatus(newDoctorGroupEvents, EventStatus.INVALID.getValue());
        doctorGroupTrackList.forEach(doctorGroupTrack -> doctorGroupTrackDao.update(doctorGroupTrack));
        //回滚关联关系
        doctorEventRelationDao.updateStatusUnderHandling(oldDoctorGroupEvents, DoctorEventRelation.Status.VALID.getValue());
        doctorEventRelationDao.batchUpdateStatus(newDoctorGroupEvents, DoctorEventRelation.Status.INVALID.getValue());
        return status;
    }

    @Transactional
    public void reElicitGroupEventByGroupId(Long groupId) {
        List<DoctorGroupEvent> groupEvents = doctorGroupEventDao.findLinkedGroupEventsByGroupId(groupId);
        if(Arguments.isNullOrEmpty(groupEvents)){
            log.error("group events info broken, groupId: {}", groupId);
            throw new InvalidException("group.events.info.broken", groupId);
        }
        DoctorGroupTrack track = doctorGroupTrackDao.findByGroupId(groupId);
        if(Arguments.isNull(track)){
            log.error("group track info broken, groupId: {}", groupId);
            throw new InvalidException("group.track.info.broken", groupId);
        }
        DoctorGroupTrack newTrack = new DoctorGroupTrack();
        newTrack.setId(track.getId());

        Long fromEventId = 0L;
        doctorGroupSnapshotDao.deleteByGroupId(groupId);
        for(DoctorGroupEvent doctorGroupEvent: groupEvents) {
            Long newGroupEventId = null;
            switch(GroupEventType.from(doctorGroupEvent.getType())){
                case NEW:
                    newTrack.setQuantity(0);
                    newGroupEventId = doctorGroupEvent.getId();
                    break;
                case MOVE_IN:
                    setAvgDayAge(newTrack, doctorGroupEvent);
                    newTrack.setQuantity(MoreObjects.firstNonNull(newTrack.getQuantity(), 0) + doctorGroupEvent.getQuantity());
                    DoctorMoveInGroupEvent moveInEvent = JSON_MAPPER.fromJson(doctorGroupEvent.getExtra(), DoctorMoveInGroupEvent.class);
                    if(!Arguments.isNull(doctorGroupEvent.getRelPigEventId()) || (!Arguments.isNull(doctorGroupEvent.getRelGroupEventId()) && Objects.equals(newGroupEventId, doctorGroupEvent.getRelGroupEventId()))){
                        newTrack.setNest(MoreObjects.firstNonNull(newTrack.getNest(), 0) + 1);
                        newTrack.setLiveQty(MoreObjects.firstNonNull(newTrack.getLiveQty(), 0) + doctorGroupEvent.getQuantity());
                        newTrack.setHealthyQty(MoreObjects.firstNonNull(newTrack.getHealthyQty(), 0) + moveInEvent.getHealthyQty());
                        newTrack.setUnweanQty(MoreObjects.firstNonNull(newTrack.getUnweanQty(), 0) + doctorGroupEvent.getQuantity());
                        newTrack.setBirthWeight(MoreObjects.firstNonNull(newTrack.getBirthWeight(), 0d) + doctorGroupEvent.getWeight());
                    }
                    break;
                case CHANGE:
                    if(!Arguments.isNull(doctorGroupEvent.getRelPigEventId())){
                        newTrack.setUnweanQty(newTrack.getUnweanQty() -  doctorGroupEvent.getQuantity());
                    }
                    newTrack.setQuantity(newTrack.getQuantity() - doctorGroupEvent.getQuantity());
                    newTrack.setAvgDayAge(DateUtil.getDeltaDaysAbs(doctorGroupEvent.getEventAt(), MoreObjects.firstNonNull(newTrack.getBirthDate(), doctorGroupEvent.getEventAt())));
                    break;
                case TRANS_GROUP:
                    if(!Arguments.isNull(doctorGroupEvent.getRelPigEventId())){
                        newTrack.setNest(newTrack.getNest() - 1);
                        newTrack.setLiveQty(EventUtil.plusInt(newTrack.getLiveQty(), - doctorGroupEvent.getQuantity()));
                        newTrack.setHealthyQty(newTrack.getLiveQty() - MoreObjects.firstNonNull(newTrack.getWeakQty(), 0));
                        newTrack.setUnweanQty(EventUtil.plusInt(newTrack.getUnweanQty(), -doctorGroupEvent.getQuantity()));
                        newTrack.setBirthWeight(EventUtil.plusDouble(newTrack.getBirthWeight(), - doctorGroupEvent.getAvgWeight() * doctorGroupEvent.getQuantity()));
                    }
                    newTrack.setQuantity(newTrack.getQuantity() - doctorGroupEvent.getQuantity());
                    newTrack.setAvgDayAge(DateUtil.getDeltaDaysAbs(doctorGroupEvent.getEventAt(), MoreObjects.firstNonNull(newTrack.getBirthDate(), doctorGroupEvent.getEventAt())));
                    break;
                case TURN_SEED:
                    newTrack.setQuantity(newTrack.getQuantity() - 1);
                    newTrack.setAvgDayAge(DateUtil.getDeltaDaysAbs(doctorGroupEvent.getEventAt(), MoreObjects.firstNonNull(newTrack.getBirthDate(), doctorGroupEvent.getEventAt())));
//                    newTrack.setBoarQty(getBoarQty(DoctorPig.PigSex.from(newTrack.getSex()), newTrack.getBoarQty()));
//                    newTrack.setSowQty(newTrack.getQuantity() - newTrack.getBoarQty());
                    break;
                case LIVE_STOCK:
                    break;
                case DISEASE:
                    break;
                case ANTIEPIDEMIC:
                    break;
                case TRANS_FARM:
                    newTrack.setQuantity(newTrack.getQuantity() - doctorGroupEvent.getQuantity());
                    newTrack.setAvgDayAge(DateUtil.getDeltaDaysAbs(doctorGroupEvent.getEventAt(), MoreObjects.firstNonNull(newTrack.getBirthDate(), doctorGroupEvent.getEventAt())));
//                    newTrack.setBoarQty(getBoarQty(DoctorPig.PigSex.from(newTrack.getSex()), newTrack.getBoarQty()));
//                    newTrack.setSowQty(newTrack.getQuantity() - newTrack.getBoarQty());
                    break;
                case CLOSE:
                    newTrack.setAvgDayAge(DateUtil.getDeltaDaysAbs(doctorGroupEvent.getEventAt(), MoreObjects.firstNonNull(newTrack.getBirthDate(), doctorGroupEvent.getEventAt())));
                    break;
                case WEAN:
                    newTrack.setWeanQty(MoreObjects.firstNonNull(newTrack.getWeanQty(), 0) + doctorGroupEvent.getQuantity());
                    newTrack.setUnweanQty(newTrack.getUnweanQty() - doctorGroupEvent.getQuantity());
                    newTrack.setAvgDayAge(DateUtil.getDeltaDaysAbs(doctorGroupEvent.getEventAt(), MoreObjects.firstNonNull(newTrack.getBirthDate(), doctorGroupEvent.getEventAt())));
                    break;
            }

            if(newTrack.getQuantity() < 0){
                log.error("group event info broken, quantity not enough, groupId: {}", groupId);
                throw new InvalidException("group.quantity.not.enough", groupId);
            }

            newTrack.setRelEventId(doctorGroupEvent.getId());
            createSnapshots(fromEventId, doctorGroupEvent, newTrack);
            fromEventId = doctorGroupEvent.getId();
        }
        doctorGroupTrackDao.update(newTrack);
        //track.quantity == 0 最后一个事件不是关闭猪群事件
        if(Objects.equals(newTrack.getQuantity(), 0) && !Objects.equals(groupEvents.get(groupEvents.size() - 1).getType(), GroupEventType.CLOSE.getValue())){
            closeGroupEvent(groupEvents.get(groupEvents.size() - 1));
        }

    }

    private void createSnapshots(Long fromEventId, DoctorGroupEvent doctorGroupEvent, DoctorGroupTrack newTrack) {
        DoctorGroupSnapshot snapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(doctorGroupEvent.getId());
        if(!Arguments.isNull(snapshot)){
            doctorGroupSnapshotDao.delete(snapshot.getId());
        }
        DoctorGroupSnapshot newSnapshot = new DoctorGroupSnapshot();
        newSnapshot.setGroupId(doctorGroupEvent.getGroupId());
        newSnapshot.setFromEventId(fromEventId);
        newSnapshot.setToEventId(doctorGroupEvent.getId());
        DoctorGroupSnapShotInfo toInfo = DoctorGroupSnapShotInfo.builder().group(doctorGroupDao.findById(doctorGroupEvent.getGroupId()))
                .groupEvent(doctorGroupEvent)
                .groupTrack(newTrack).build();
        newSnapshot.setToInfo(JSON_MAPPER.toJson(toInfo));
        doctorGroupSnapshotDao.create(newSnapshot);
    }

    public void closeGroupEvent(DoctorGroupEvent doctorGroupEvent) {
        DoctorGroup group = doctorGroupDao.findById(doctorGroupEvent.getGroupId());
        group.setStatus(DoctorGroup.Status.CLOSED.getValue());
        doctorGroupDao.update(group);
        doctorGroupEvent.setType(GroupEventType.CLOSE.getValue());
        doctorGroupEvent.setName(GroupEventType.CLOSE.getDesc());
        doctorGroupEvent.setDesc("【系统自动】");
        doctorGroupEvent.setIsAuto(IsOrNot.YES.getValue());
        doctorGroupEvent.setRelGroupEventId(doctorGroupEvent.getId());
        doctorGroupEvent.setExtra(null);
        doctorGroupEventDao.create(doctorGroupEvent);
    }

    private void setAvgDayAge(DoctorGroupTrack newTrack, DoctorGroupEvent doctorGroupEvent) {
        int avgDayAge = getAvgDayAge(newTrack, doctorGroupEvent);
        newTrack.setBirthDate(new DateTime(doctorGroupEvent.getEventAt()).minusDays(avgDayAge).toDate());
        newTrack.setAvgDayAge(avgDayAge);
    }

    public int getAvgDayAge(DoctorGroupTrack newTrack, DoctorGroupEvent doctorGroupEvent){
        int deltaDays = DateUtil.getDeltaDaysAbs(doctorGroupEvent.getEventAt(), MoreObjects.firstNonNull(newTrack.getBirthDate(), doctorGroupEvent.getEventAt()));
        int avgDayAge = EventUtil.getAvgDayAge(MoreObjects.firstNonNull(newTrack.getAvgDayAge(), 0) + deltaDays, MoreObjects.firstNonNull(newTrack.getQuantity(), 0), doctorGroupEvent.getAvgDayAge(), doctorGroupEvent.getQuantity());
        return avgDayAge;
    }

    public Boolean triggerPigEvents(DoctorGroupEvent doctorGroupEvent){
        DoctorPigEvent oldPigEvent = doctorPigEventDao.findById(doctorGroupEvent.getRelPigEventId());
        if(Arguments.isNull(oldPigEvent)){
            log.info("find pigEvent failed, doctorGroupEvent = {}", doctorGroupEvent);
            throw new JsonResponseException("find.pigEvent.failed");
        }
        DoctorBasicInputInfoDto basicDto = new DoctorBasicInputInfoDto();
        DoctorFarmEntryDto farmEntryDto = new DoctorFarmEntryDto();
        DoctorTurnSeedGroupEvent trunSeedGroupEvent = JSON_MAPPER.fromJson(doctorGroupEvent.getExtra(), DoctorTurnSeedGroupEvent.class);

        ///恭母猪进场字段
        if (Objects.equals(getSex(trunSeedGroupEvent.getToBarnType()), DoctorPig.PigSex.BOAR)) {
            farmEntryDto.setPigType(DoctorPig.PigSex.BOAR.getKey());
            farmEntryDto.setBoarType(BoarEntryType.HGZ.getKey());
            farmEntryDto.setBoarTypeName(BoarEntryType.HGZ.getCode());
        } else {
            farmEntryDto.setPigType(DoctorPig.PigSex.SOW.getKey());
            farmEntryDto.setParity(1);
            farmEntryDto.setEarCode(trunSeedGroupEvent.getEarCode());
        }

        //基本信息
        farmEntryDto.setRelGroupEventId(doctorGroupEvent.getId());
        farmEntryDto.setPigId(oldPigEvent.getPigId());
        farmEntryDto.setPigCode(trunSeedGroupEvent.getPigCode());
        farmEntryDto.setBarnId(trunSeedGroupEvent.getToBarnId());
        farmEntryDto.setBarnName(trunSeedGroupEvent.getToBarnName());
        basicDto.setFarmId(doctorGroupEvent.getFarmId());
        basicDto.setFarmName(doctorGroupEvent.getFarmName());
        basicDto.setOrgId(doctorGroupEvent.getOrgId());
        basicDto.setOrgName(doctorGroupEvent.getOrgName());
        farmEntryDto.setEventType(PigEvent.ENTRY.getKey());
        farmEntryDto.setEventName(PigEvent.ENTRY.getName());
        farmEntryDto.setEventDesc(PigEvent.ENTRY.getDesc());
        basicDto.setStaffId(doctorGroupEvent.getCreatorId());
        basicDto.setStaffName(doctorGroupEvent.getCreatorName());
        farmEntryDto.setIsAuto(IsOrNot.YES.getValue());

        //进场信息
        //farmEntryDto.setPigType(basicDto.getPigType());
        farmEntryDto.setPigCode(trunSeedGroupEvent.getPigCode());
        farmEntryDto.setBirthday(DateUtil.toDate(trunSeedGroupEvent.getBirthDate()));
        farmEntryDto.setInFarmDate(doctorGroupEvent.getEventAt());
        farmEntryDto.setBarnId(trunSeedGroupEvent.getToBarnId());
        farmEntryDto.setBarnName(trunSeedGroupEvent.getToBarnName());
        farmEntryDto.setSource(PigSource.LOCAL.getKey());
        farmEntryDto.setBreed(trunSeedGroupEvent.getBreedId());
        farmEntryDto.setBreedName(trunSeedGroupEvent.getBreedName());
        farmEntryDto.setBreedType(trunSeedGroupEvent.getGeneticId());
        farmEntryDto.setBreedTypeName(trunSeedGroupEvent.getGeneticName());
        farmEntryDto.setMotherCode(trunSeedGroupEvent.getMotherEarCode());
        farmEntryDto.setEarCode(trunSeedGroupEvent.getEarCode());
        farmEntryDto.setWeight(trunSeedGroupEvent.getWeight());

        DoctorPigEvent pigEvent = doctorEntryHandler.buildPigEvent(basicDto, farmEntryDto);
        return true;
    }

    //获取转种猪性别
    private static DoctorPig.PigSex getSex(Integer toBarnType) {
        if (PigType.MATING_TYPES.contains(toBarnType)) {
            return DoctorPig.PigSex.SOW;
        }
        return DoctorPig.PigSex.BOAR;
    }
}
