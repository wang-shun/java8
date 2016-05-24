package io.terminus.doctor.event.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.constants.DoctorFarmEntrySowConstants;
import io.terminus.doctor.event.constants.DoctorPigSnapshotConstants;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪事件信息录入管理过程
 */
@Component
@Slf4j
public class DoctorPigEventManager {

    private final DoctorPigDao doctorPigDao;

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorPigSnapshotDao doctorPigSnapshotDao;

    @Autowired
    public DoctorPigEventManager(DoctorPigDao doctorPigDao,
                                 DoctorPigEventDao doctorPigEventDao,
                                 DoctorPigTrackDao doctorPigTrackDao,
                                 DoctorPigSnapshotDao doctorPigSnapshotDao){
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
    }

    private static final String REMARK = "remark";


    @Transactional
    public Boolean rollBackPigEvent(Long pigEventId){

        // delete event
        checkState(doctorPigEventDao.delete(pigEventId), "delete.pigEventById.fail");

        // roll back track info
        DoctorPigSnapshot doctorPigSnapshot = doctorPigSnapshotDao.queryByEventId(pigEventId);
        DoctorPigTrack doctorPigTrack =
        JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(
                String.valueOf(doctorPigSnapshot.getPigInfoMap().get(DoctorPigSnapshotConstants.PIG_TRACK)),
                DoctorPigTrack.class);
        checkState(doctorPigTrackDao.update(doctorPigTrack), "update.snapshot.fail");

        //delete snapshot
        checkState(doctorPigSnapshotDao.deleteByEventId(pigEventId), "delete.snapshot.error");
        return Boolean.FALSE;
    }

    /**
     * 创建（公猪， 母猪 疾病）事件信息内容
     * @param dto
     * @param basic
     * @param pigType
     * @return
     */
    @Transactional
    public Boolean createDiseaseEvent(DoctorDiseaseDto dto, DoctorBasicInputInfoDto basic, Integer pigType){
        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(dto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, basic, extra),track->{
            track.setRemark(dto.getDiseaseRemark());
            track.addAllExtraMap(extra);
            return track;
        });
        return true;
    }

    @Transactional
    public Boolean createVaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto basic, Integer pigType){
        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(doctorVaccinationDto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, basic,extra), track->{
            track.setRemark(doctorVaccinationDto.getVaccinationRemark());
            track.addAllExtraMap(extra);
            return track;
        });
        return true;
    }

    @Transactional
    public Boolean createConditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType){
        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(doctorConditionDto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, doctorBasicInputInfoDto, extra), doctorPigTrack -> {
            doctorPigTrack.setRemark(doctorConditionDto.getConditionRemark());
            doctorPigTrack.setWeight(doctorConditionDto.getConditionWeight());
            doctorPigTrack.addAllExtraMap(extra);
            return doctorPigTrack;
        });
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean createChgLocationEvent(DoctorChgLocationDto dto, DoctorBasicInputInfoDto basic, Integer pigType){
        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(dto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, basic, extra), track->{
            track.setCurrentBarnId(dto.getChgLocationToBarnId());
            track.setCurrentBarnName(dto.getChgLocationToBarnName());
            track.addAllExtraMap(extra);
            return track;
        });
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean createChgFarmLocationEvent(DoctorChgFarmDto dto, DoctorBasicInputInfoDto basic, Integer pigType){
        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(dto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, basic, extra), track->{
            track.addAllExtraMap(extra);
            return track;
        });
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean createRemovalEvent(DoctorRemovalDto dto, DoctorBasicInputInfoDto basic, Integer pigType){
        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(dto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, basic, extra), track->{
            track.addAllExtraMap(extra);
            return track;
        });
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean createBoarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto basic, Integer pigType){
        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(doctorSemenDto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, basic, extra), track->{
            track.setWeight(doctorSemenDto.getWeight());
            track.addAllExtraMap(extra);
            return track;
        });
        return Boolean.TRUE;
    }

    public <T> Boolean createPigEventOnlyExtra(T dto, DoctorBasicInputInfoDto basicInputInfoDto, Integer pigType){
        Map<String, Object> extra = Maps.newHashMap();
        BeanMapper.copy(dto, extra);
        createAllEvent(buildAllPigDoctorEvent(pigType, basicInputInfoDto, extra), track->{
            track.addAllExtraMap(extra);
            return track;
        });
        return Boolean.TRUE;
    }

    /**
     * 猪进厂事件信息
     * @param basic
     * @param dto
     * @param pigType
     * @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     * @return
     */
    @Transactional
    public Boolean pigEntryEvent(DoctorBasicInputInfoDto basic, DoctorFarmEntryDto dto, Integer pigType){

        createEntryEvent(
                buildDoctorPig(dto, basic, pigType),
                buildDoctorPigEntryEvent(basic, dto, pigType),
                buildEntryFarmPigDoctorTrack(dto, basic));
        return Boolean.TRUE;
    }

    /**
     * 录入进场事件信息
     * @param doctorPig
     * @param doctorPigEvent
     * @param doctorPigTrack
     * @param doctorPigSnapshot
     * @return 对应的录入结果
     */
    private Boolean createEntryEvent(DoctorPig doctorPig, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack){
        // pig create
        doctorPigDao.create(doctorPig);

        // event create
        doctorPigEvent.setPigId(doctorPig.getId());
        doctorPigEventDao.create(doctorPigEvent);

        // track create
        doctorPigTrack.setPigId(doctorPig.getId());
        doctorPigTrack.setRelEventId(doctorPigEvent.getId());
        doctorPigTrackDao.create(doctorPigTrack);

        // snapshot create
        DoctorPigSnapshot doctorPigSnapshot = DoctorPigSnapshot.builder()
                .pigId(doctorPig.getId()).farmId(doctorPig.getFarmId()).orgId(doctorPig.getOrgId()).eventId(doctorPigEvent.getId())
                .build();
        doctorPigSnapshot.setPigInfoMap(ImmutableMap.of(DoctorPigSnapshotConstants.PIG_TRACK,JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack)));
        doctorPigSnapshotDao.create(doctorPigSnapshot);
        return Boolean.TRUE;
    }


    private Boolean createAllEvent(DoctorPigEvent doctorPigEvent, Function<DoctorPigTrack,DoctorPigTrack> callPigTrackBack){
        // create event
        doctorPigEventDao.create(doctorPigEvent);

        // update track info
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());
        DoctorPigTrack refreshPigTrack = callPigTrackBack.apply(doctorPigTrack);
        doctorPigTrackDao.update(refreshPigTrack);

        // create snapshot info
        // snapshot create
        DoctorPigSnapshot doctorPigSnapshot = DoctorPigSnapshot.builder()
                .pigId(doctorPigEvent.getId()).farmId(doctorPigEvent.getFarmId()).orgId(doctorPigEvent.getOrgId()).eventId(doctorPigEvent.getId())
                .build();
        doctorPigSnapshot.setPigInfoMap(ImmutableMap.of(DoctorPigSnapshotConstants.PIG_TRACK,JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack)));
        doctorPigSnapshotDao.create(doctorPigSnapshot);
        return Boolean.TRUE;
    }

    public DoctorPigEvent buildAllPigDoctorEvent(Integer pigType, DoctorBasicInputInfoDto basic, Map<String,Object> extra){
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName()).farmId(basic.getFarmId()).farmName(basic.getFarmName()).pigId(basic.getPigId()).pigCode(basic.getPigCode())
                .eventAt(DateTime.now().toDate()).type(basic.getEventType()).kind(pigType).name(basic.getEventName()).desc(basic.getEventDesc()).relEventId(basic.getRelEventId())
                .barnId(basic.getBarnId()).barnName(basic.getBarnName())
                .outId("") //TODO uuid generate method
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        doctorPigEvent.setExtraMap(extra);
        return doctorPigEvent;
    }

    /**
     * build pig event 构建进厂事件信息
     * @param basic
     * @param dto
     * @param pigType
     * @return
     */
    private DoctorPigEvent buildDoctorPigEntryEvent(DoctorBasicInputInfoDto basic, DoctorFarmEntryDto dto, Integer pigType){
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName()).farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigCode(dto.getPigCode()).eventAt(DateTime.now().toDate())
                .type(basic.getEventType()).kind(pigType).name(basic.getEventName()).desc(basic.getEventDesc())
                .barnId(dto.getBarnId()).barnName(dto.getBarnName()).relEventId(basic.getRelEventId())
                .outId(UUID.randomUUID().toString()).remark(dto.getMark())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        return doctorPigEvent;
    }

    /**
     * 构建猪进厂 Track 信息表 TODO 获取进厂事件信息
     * @param dto
     * @param basic
     * @return
     */
    private DoctorPigTrack buildEntryFarmPigDoctorTrack(DoctorFarmEntryDto dto, DoctorBasicInputInfoDto basic){
        return DoctorPigTrack.builder().farmId(basic.getFarmId())
                .status(-1)  // TODO status to wait base service
                .currentBarnId(dto.getBarnId()).currentBarnName(dto.getBarnName())
                .currentParity(dto.getParity())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
    }

    /**
     * 构建DoctorPig
     * @param dto
     * @param basic
     * @return
     */
    private DoctorPig buildDoctorPig(DoctorFarmEntryDto dto, DoctorBasicInputInfoDto basic, Integer pigType){

        if(isNull(basic.getFarmId())||isNull(dto.getPigCode())){
            return null;
        }

        DoctorPig doctorPig = DoctorPig.builder()
                .farmId(basic.getFarmId()).farmName(basic.getFarmName()).orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .outId(UUID.randomUUID().toString()).pigCode(dto.getPigCode()).pigType(pigType).pigFatherId(dto.getFatherId()).pigMotherId(dto.getMotherId())
                .source(dto.getSource()).birthDate(dto.getBirthday()).inFarmDate(dto.getInFarmDate()).inFarmDayAge(Years.yearsBetween(new DateTime(dto.getBirthday()),DateTime.now()).getYears())
                .initBarnId(dto.getBarnId()).initBarnName(dto.getBarnName()).breedId(dto.getBreed()).breedName(dto.getBreedName()).geneticId(dto.getBreedType()).geneticName(dto.getBreedTypeName())
                .remark(dto.getMark()).creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if(Objects.equals(pigType, DoctorPig.PIG_TYPE.SOW.getKey())){
            // add sow pig info
            Map<String,Object> extraMapInfo = Maps.newHashMap();
            extraMapInfo.put(DoctorFarmEntrySowConstants.EAR_CODE, dto.getEarCode());
            extraMapInfo.put(DoctorFarmEntrySowConstants.FIRST_PARITY, dto.getParity());
            extraMapInfo.put(DoctorFarmEntrySowConstants.LEFT_COUNT, dto.getLeft());
            extraMapInfo.put(DoctorFarmEntrySowConstants.RIGHT_COUNT, dto.getRight());
            doctorPig.setExtraMap(extraMapInfo);
        }
        return doctorPig;
    }
}
