package io.terminus.doctor.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.event.constants.DoctorFarmEntrySowConstants;
import io.terminus.doctor.event.constants.DoctorPigSnapshotConstants;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarStatus;
import io.terminus.doctor.event.enums.SowStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorEntryFlowHandler extends HandlerAware {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private final DoctorPigDao doctorPigDao;

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorPigSnapshotDao doctorPigSnapshotDao;

    private final DoctorRevertLogDao doctorRevertLogDao;

    @Autowired
    public DoctorEntryFlowHandler(DoctorPigDao doctorPigDao,
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
    public void handle(Execution execution) {
        try{
            // get data
            Map<String,String> flowDataMap = OBJECT_MAPPER.readValue(execution.getFlowData(), JacksonType.MAP_OF_STRING);
            DoctorBasicInputInfoDto basic = OBJECT_MAPPER.readValue(flowDataMap.get("basic"), DoctorBasicInputInfoDto.class);
            DoctorFarmEntryDto doctorFarmEntryDto = OBJECT_MAPPER.readValue(flowDataMap.get("dto"), DoctorFarmEntryDto.class);

            // build bean
            DoctorPig doctorPig = buildDoctorPig(doctorFarmEntryDto, basic);
            DoctorPigEvent doctorPigEvent = buildDoctorPigEntryEvent(basic, doctorFarmEntryDto);
            DoctorPigTrack doctorPigTrack = buildEntryFarmPigDoctorTrack(doctorFarmEntryDto, basic);

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
            doctorPigSnapshot.setPigInfoMap(ImmutableMap.of(DoctorPigSnapshotConstants.PIG_TRACK, JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack)));
            doctorPigSnapshotDao.create(doctorPigSnapshot);

            flowDataMap.put("entryResult",
                    JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
                            ImmutableMap.of("doctorPig", doctorPig.getId(),"eventId",doctorPigEvent.getId(),
                                    "doctorPigTrack", doctorPigTrack.getId(),"snapshotId", doctorPigSnapshot.getId())
                    ));
            execution.setFlowData(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(flowDataMap));
        }catch (Exception e){
            log.error("doctor abstract entry flow handle fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("sow.entryFarm.error");
        }
    }

    /**
     * 构建猪进厂 Track 信息表 TODO 获取进厂事件信息
     * @param dto
     * @param basic
     * @return
     */
    private DoctorPigTrack buildEntryFarmPigDoctorTrack(DoctorFarmEntryDto dto, DoctorBasicInputInfoDto basic){

        DoctorPigTrack doctorPigTrack = DoctorPigTrack.builder().farmId(basic.getFarmId())
                .currentBarnId(dto.getBarnId()).currentBarnName(dto.getBarnName())
                .currentParity(dto.getParity())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if(Objects.equals(basic.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
            doctorPigTrack.setStatus(SowStatus.Entry.getKey());
        }else if(Objects.equals(basic.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())) {
            doctorPigTrack.setStatus(BoarStatus.ENTRY.getKey());
        }else {
            throw new IllegalStateException("input.pigType.error");
        }
        return doctorPigTrack;
    }

    /**
     * build pig event 构建进厂事件信息
     * @param basic
     * @param dto
     * @param pigType
     * @return
     */
    private DoctorPigEvent buildDoctorPigEntryEvent(DoctorBasicInputInfoDto basic, DoctorFarmEntryDto dto){
        DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName()).farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigCode(dto.getPigCode()).eventAt(DateTime.now().toDate())
                .type(basic.getEventType()).kind(basic.getPigType()).name(basic.getEventName()).desc(basic.getEventDesc())
                .barnId(dto.getBarnId()).barnName(dto.getBarnName()).relEventId(basic.getRelEventId())
                .outId(UUID.randomUUID().toString()).remark(dto.getMark())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        return doctorPigEvent;
    }

    /**
     * 构建DoctorPig
     * @param dto
     * @param basic
     * @return
     */
    private DoctorPig buildDoctorPig(DoctorFarmEntryDto dto, DoctorBasicInputInfoDto basic){

        if(isNull(basic.getFarmId())||isNull(dto.getPigCode())){
            return null;
        }

        DoctorPig doctorPig = DoctorPig.builder()
                .farmId(basic.getFarmId()).farmName(basic.getFarmName()).orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .outId(UUID.randomUUID().toString()).pigCode(dto.getPigCode()).pigType(basic.getPigType()).pigFatherId(dto.getFatherId()).pigMotherId(dto.getMotherId())
                .source(dto.getSource()).birthDate(dto.getBirthday()).inFarmDate(dto.getInFarmDate()).inFarmDayAge(Years.yearsBetween(new DateTime(dto.getBirthday()), DateTime.now()).getYears())
                .initBarnId(dto.getBarnId()).initBarnName(dto.getBarnName()).breedId(dto.getBreed()).breedName(dto.getBreedName()).geneticId(dto.getBreedType()).geneticName(dto.getBreedTypeName())
                .remark(dto.getMark()).creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if(Objects.equals(basic.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
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
