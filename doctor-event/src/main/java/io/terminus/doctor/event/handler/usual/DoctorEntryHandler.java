package io.terminus.doctor.event.handler.usual;

import com.google.common.collect.Maps;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.constants.DoctorFarmEntryConstants;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class DoctorEntryHandler extends DoctorAbstractEventHandler {

    @Autowired
    private  DoctorPigInfoCache doctorPigInfoCache;

    @Override
    public void handleCheck(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        checkState(doctorPigDao.findPigByFarmIdAndPigCode(basic.getFarmId(), inputDto.getPigCode()) == null, "猪号" + inputDto.getPigCode() + "已存在");
    }


    @Override
    public void handle(List<DoctorEventInfo> doctorEventInfoList, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
//        try {
        DoctorFarmEntryDto farmEntryDto = (DoctorFarmEntryDto) inputDto;

        DoctorPig doctorPig = buildDoctorPig(farmEntryDto, basic);
        farmEntryDto.setPigId(doctorPig.getId());
        doctorPigDao.create(doctorPig);
        inputDto.setPigId(doctorPig.getId());


        //2.创建事件
        DoctorPigEvent doctorPigEvent = buildPigEvent(basic, inputDto);
        doctorPigEventDao.create(doctorPigEvent);

        //3.创建或更新track
        DoctorPigTrack doctorPigTrack = createOrUpdatePigTrack(basic, inputDto);

        doctorPigTrackDao.create(doctorPigTrack);

        //1.创建镜像
        DoctorPigTrack pigSnapshotTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorPigEvent pigSnapshotEvent = doctorPigEventDao.queryLastPigEventById(inputDto.getPigId());
        DoctorPigSnapshot doctorPigSnapshot = createPigSnapshot(pigSnapshotTrack, pigSnapshotEvent, doctorPigEvent.getId());
        doctorPigSnapshotDao.create(doctorPigSnapshot);
        //4.特殊处理
        specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);

        //5.记录发生的事件信息
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
                .eventType(basic.getEventType())
                .build();
        doctorEventInfoList.add(doctorEventInfo);

        //6.触发事件
        triggerEvent(doctorEventInfoList, doctorPigEvent, doctorPigTrack, inputDto, basic);
            // event create
//                doctorPigEvent.setRelGroupEventId(basic.getRelGroupEventId());
//                doctorPigEvent.setPigId(doctorPig.getId());
//                doctorPigEventDao.create(doctorPigEvent);

//                // track create
//                doctorPigTrack.setPigId(doctorPig.getId());
//                doctorPigTrack.setPigType(doctorPig.getPigType());
//                doctorPigTrack.addPigEvent(doctorPig.getPigType(), doctorPigEvent.getId());
//                doctorPigTrackDao.create(doctorPigTrack);

//                doctorPigEvent.setPigStatusAfter(doctorPigTrack.getStatus());
//                //添加时间发生之前母猪的胎次
//                doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
//                doctorPigEventDao.update(doctorPigEvent);

//                // snapshot create
//                DoctorPigSnapshot doctorPigSnapshot = DoctorPigSnapshot.builder()
//                        .pigId(doctorPig.getId()).farmId(doctorPig.getFarmId()).orgId(doctorPig.getOrgId()).eventId(doctorPigEvent.getId())
//                        .build();
//
//                DoctorPigSnapShotInfo snapShotInfo = DoctorPigSnapShotInfo.builder()
//                        .pig(doctorPig)
//                        .pigTrack(doctorPigTrack)
//                        .pigEvent(doctorPigEvent)
//                        .build();
//
//                doctorPigSnapshot.setPigInfo(JSON_MAPPER.toJson(snapShotInfo));
//                doctorPigSnapshotDao.create(doctorPigSnapshot);
//
//                doctorPigInfoCache.addPigCodeToFarm(doctorPig.getFarmId(), doctorPig.getPigCode());
//        }catch(RuntimeException e){
//            throw new ServiceException(e.getMessage());
//        }catch (Exception e){
//            log.error("doctor abstract entry flow handle fail, cause:{}", Throwables.getStackTraceAsString(e));
//            throw new IllegalStateException("entry.handler.exception");
//        }
    }

    @Override
    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        super.specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);
        doctorPigInfoCache.addPigCodeToFarm(basic.getFarmId(), inputDto.getPigCode());
    }

    @Override
    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent =  super.buildPigEvent(basic, inputDto);
        DoctorFarmEntryDto farmEntryDto = (DoctorFarmEntryDto) inputDto;
        doctorPigEvent.setParity(farmEntryDto.getParity());
        return doctorPigEvent;
    }

    /**
     * 构建猪进厂 Track 信息表
     *
     * @param basic
     * @return
     */
    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorFarmEntryDto dto = (DoctorFarmEntryDto) inputDto;
        DoctorPigTrack doctorPigTrack = DoctorPigTrack.builder().farmId(basic.getFarmId()).pigType(inputDto.getPigType())
                .isRemoval(IsOrNot.NO.getValue()).currentMatingCount(0)
                .pigId(inputDto.getPigId()).pigType(inputDto.getPigType())
                .currentBarnId(dto.getBarnId()).currentBarnName(dto.getBarnName())
                .currentParity(dto.getParity())
                .weight(dto.getWeight())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if (Objects.equals(inputDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())) {
            doctorPigTrack.setStatus(PigStatus.Entry.getKey());
        } else if (Objects.equals(inputDto.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())) {
            doctorPigTrack.setStatus(PigStatus.BOAR_ENTRY.getKey());
        } else {
            throw new IllegalStateException("input.pigType.error");
        }
        //添加进场到配种标志位
        doctorPigTrack.addAllExtraMap(MapBuilder.<String, Object>of().put("enterToMate", true).map());
        return doctorPigTrack;
    }

//    /**
//     * build pig event 构建进厂事件信息
//     *
//     * @param basic
//     * @param dto
//     * @return
//     */
//    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, DoctorFarmEntryDto dto) {
//        super.buildPigEvent(basic, dto);
//        return DoctorPigEvent.builder()
//                .orgId(basic.getOrgId()).orgName(basic.getOrgName()).farmId(basic.getFarmId()).farmName(basic.getFarmName())
//                .pigCode(dto.getPigCode()).eventAt(dto.eventAt())
//                .type(basic.getEventType()).kind(basic.getPigType()).name(basic.getEventName()).desc(basic.generateEventDescFromExtra(dto))
//                .barnId(dto.getBarnId()).barnName(dto.getBarnName()).relEventId(basic.getRelEventId())
//                .remark(dto.getEntryMark()).creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
//                .isAuto(MoreObjects.firstNonNull(basic.getIsAuto(), IsOrNot.NO.getValue()))
//                .npd(0)
//                .dpnpd(0)
//                .pfnpd(0)
//                .plnpd(0)
//                .psnpd(0)
//                .pynpd(0)
//                .ptnpd(0)
//                .jpnpd(0)
//                .build();
//    }

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
                .pigCode(dto.getPigCode()).pigType(dto.getPigType())
                .isRemoval(IsOrNot.NO.getValue())
                .pigFatherCode(dto.getFatherCode()).pigMotherCode(dto.getMotherCode())
                .source(dto.getSource()).birthDate(generateEventAt(dto.getBirthday())).inFarmDate(generateEventAt(dto.getInFarmDate())).inFarmDayAge(Years.yearsBetween(new DateTime(dto.getBirthday()), DateTime.now()).getYears())
                .initBarnId(dto.getBarnId()).initBarnName(dto.getBarnName()).breedId(dto.getBreed()).breedName(dto.getBreedName()).geneticId(dto.getBreedType()).geneticName(dto.getBreedTypeName())
                .remark(dto.getEntryMark()).creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if (Objects.equals(dto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())) {
            // add sow pig info
            Map<String, Object> extraMapInfo = Maps.newHashMap();
            extraMapInfo.put(DoctorFarmEntryConstants.EAR_CODE, dto.getEarCode());
            extraMapInfo.put(DoctorFarmEntryConstants.FIRST_PARITY, dto.getParity());
            extraMapInfo.put(DoctorFarmEntryConstants.LEFT_COUNT, dto.getLeft());
            extraMapInfo.put(DoctorFarmEntryConstants.RIGHT_COUNT, dto.getRight());
            doctorPig.setExtraMap(extraMapInfo);
        } else if (Objects.equals(dto.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())) {
            Map<String, Object> extraMap = Maps.newHashMap();
            extraMap.put(DoctorFarmEntryConstants.BOAR_TYPE_ID, dto.getBoarTypeId());
            extraMap.put(DoctorFarmEntryConstants.BOAR_TYPE_NAME, dto.getBoarTypeName());
            doctorPig.setExtraMap(extraMap);
        }
        return doctorPig;
    }

}
