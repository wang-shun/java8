package io.terminus.doctor.event.handler;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.constants.DoctorFarmEntryConstants;
import io.terminus.doctor.event.constants.DoctorPigSnapshotConstants;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorEntryHandler implements DoctorEventCreateHandler {

    private final DoctorPigDao doctorPigDao;

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorPigSnapshotDao doctorPigSnapshotDao;

    private final DoctorPigInfoCache doctorPigInfoCache;

    @Autowired
    public DoctorEntryHandler(DoctorPigDao doctorPigDao,
                              DoctorPigEventDao doctorPigEventDao,
                              DoctorPigTrackDao doctorPigTrackDao,
                              DoctorPigSnapshotDao doctorPigSnapshotDao,
                              DoctorPigInfoCache doctorPigInfoCache) {
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorPigInfoCache = doctorPigInfoCache;
    }

    @Override
    public Boolean preHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        return Objects.equals(basic.getEventType(), PigEvent.ENTRY.getKey());
    }

    @Override
    public void handler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        try {
            // get data
            DoctorFarmEntryDto doctorFarmEntryDto = new DoctorFarmEntryDto();
            BeanMapper.copy(extra, doctorFarmEntryDto);

            // build bean
            DoctorPig doctorPig = buildDoctorPig(doctorFarmEntryDto, basic);
            DoctorPigEvent doctorPigEvent = buildDoctorPigEntryEvent(basic, doctorFarmEntryDto);
            DoctorPigTrack doctorPigTrack = buildEntryFarmPigDoctorTrack(doctorFarmEntryDto, basic);

            // pig create
            checkState(doctorPigInfoCache.judgePigCodeNotContain(doctorPig.getOrgId(), doctorPig.getPigCode()), "validate.pigCode.fail");
            doctorPigDao.create(doctorPig);

            // event create
            doctorPigEvent.setPigId(doctorPig.getId());
            doctorPigEventDao.create(doctorPigEvent);

            // track create
            doctorPigTrack.setPigId(doctorPig.getId());
            doctorPigTrack.setPigType(doctorPig.getPigType());
            doctorPigTrack.addPigEvent(doctorPig.getPigType(), doctorPigEvent.getId());
            doctorPigTrackDao.create(doctorPigTrack);

            doctorPigEvent.setPigStatusAfter(doctorPigTrack.getStatus());
            //添加时间发生之前母猪的胎次
            doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
            doctorPigEventDao.update(doctorPigEvent);

            // snapshot create
            DoctorPigSnapshot doctorPigSnapshot = DoctorPigSnapshot.builder()
                    .pigId(doctorPig.getId()).farmId(doctorPig.getFarmId()).orgId(doctorPig.getOrgId()).eventId(doctorPigEvent.getId())
                    .build();
            doctorPigSnapshot.setPigInfoMap(ImmutableMap.of(DoctorPigSnapshotConstants.PIG_TRACK, JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrack)));
            doctorPigSnapshotDao.create(doctorPigSnapshot);

            context.put("createEventResult",
                    JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
                            ImmutableMap.of("doctorPigId", doctorPig.getId(), "doctorEventId", doctorPigEvent.getId(),
                                    "doctorPigTrackId", doctorPigTrack.getId(), "doctorSnapshotId", doctorPigSnapshot.getId())
                    ));
            doctorPigInfoCache.addPigCodeToFarm(doctorPig.getOrgId(), doctorPig.getPigCode());
        }catch(RuntimeException e){
            throw new ServiceException(e.getMessage());
        }catch (Exception e){
            log.error("doctor abstract entry flow handle fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new IllegalStateException("entry.handler.exception");
        }
    }

    @Override
    public void afterHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {

    }

    /**
     * 构建猪进厂 Track 信息表
     *
     * @param dto
     * @param basic
     * @return
     */
    private DoctorPigTrack buildEntryFarmPigDoctorTrack(DoctorFarmEntryDto dto, DoctorBasicInputInfoDto basic) {

        DoctorPigTrack doctorPigTrack = DoctorPigTrack.builder().farmId(basic.getFarmId()).pigType(basic.getPigType())
                .isRemoval(IsOrNot.NO.getValue())
                .currentBarnId(dto.getBarnId()).currentBarnName(dto.getBarnName())
                .currentParity(dto.getParity())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if (Objects.equals(basic.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())) {
            doctorPigTrack.setStatus(PigStatus.Entry.getKey());
        } else if (Objects.equals(basic.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())) {
            doctorPigTrack.setStatus(PigStatus.BOAR_ENTRY.getKey());
        } else {
            throw new IllegalStateException("input.pigType.error");
        }
        //添加进场到配种标志位
        doctorPigTrack.addAllExtraMap(MapBuilder.<String, Object>of().put("enterToMate", true).map());
        return doctorPigTrack;
    }

    /**
     * build pig event 构建进厂事件信息
     *
     * @param basic
     * @param dto
     * @return
     */
    private DoctorPigEvent buildDoctorPigEntryEvent(DoctorBasicInputInfoDto basic, DoctorFarmEntryDto dto) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        Map<String, Object> extra = BeanMapper.convertObjectToMap(dto);
        return DoctorPigEvent.builder()
                .orgId(basic.getOrgId()).orgName(basic.getOrgName()).farmId(basic.getFarmId()).farmName(basic.getFarmName())
                .pigCode(dto.getPigCode()).eventAt(basic.generateEventAtFromExtra(extra))
                .type(basic.getEventType()).kind(basic.getPigType()).name(basic.getEventName()).desc(basic.generateEventDescFromExtra(extra))
                .barnId(dto.getBarnId()).barnName(dto.getBarnName()).relEventId(basic.getRelEventId())
                .outId(UUID.randomUUID().toString()).remark(dto.getEntryMark())
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
    }

    /**
     * 构建DoctorPig
     *
     * @param dto
     * @param basic
     * @return
     */
    private DoctorPig buildDoctorPig(DoctorFarmEntryDto dto, DoctorBasicInputInfoDto basic) {

        if (isNull(basic.getFarmId()) || isNull(dto.getPigCode())) {
            throw new IllegalArgumentException("input.farmIdPigCode.empty");
        }

        DoctorPig doctorPig = DoctorPig.builder()
                .farmId(basic.getFarmId()).farmName(basic.getFarmName()).orgId(basic.getOrgId()).orgName(basic.getOrgName())
                .outId(UUID.randomUUID().toString()).pigCode(dto.getPigCode()).pigType(basic.getPigType())
                .isRemoval(IsOrNot.NO.getValue())
                .pigFatherCode(dto.getFatherCode()).pigMotherCode(dto.getMotherCode())
                .source(dto.getSource()).birthDate(dto.getBirthday()).inFarmDate(dto.getInFarmDate()).inFarmDayAge(Years.yearsBetween(new DateTime(dto.getBirthday()), DateTime.now()).getYears())
                .initBarnId(dto.getBarnId()).initBarnName(dto.getBarnName()).breedId(dto.getBreed()).breedName(dto.getBreedName()).geneticId(dto.getBreedType()).geneticName(dto.getBreedTypeName())
                .remark(dto.getEntryMark()).creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if (Objects.equals(basic.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())) {
            // add sow pig info
            Map<String, Object> extraMapInfo = Maps.newHashMap();
            extraMapInfo.put(DoctorFarmEntryConstants.EAR_CODE, dto.getEarCode());
            extraMapInfo.put(DoctorFarmEntryConstants.FIRST_PARITY, dto.getParity());
            extraMapInfo.put(DoctorFarmEntryConstants.LEFT_COUNT, dto.getLeft());
            extraMapInfo.put(DoctorFarmEntryConstants.RIGHT_COUNT, dto.getRight());
            doctorPig.setExtraMap(extraMapInfo);
        } else if (Objects.equals(basic.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())) {
            Map<String, Object> extraMap = Maps.newHashMap();
            extraMap.put(DoctorFarmEntryConstants.BOAR_TYPE_ID, dto.getBoarTypeId());
            extraMap.put(DoctorFarmEntryConstants.BOAR_TYPE_NAME, dto.getBoarTypeName());
            doctorPig.setExtraMap(extraMap);
        }
        return doctorPig;
    }
}
