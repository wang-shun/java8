package io.terminus.doctor.event.handler.usual;

import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.constants.DoctorFarmEntryConstants;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
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

import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorEntryHandler extends DoctorAbstractEventHandler{

    @Autowired
    private  DoctorPigInfoCache doctorPigInfoCache;

    @Override
    public void handleCheck(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        expectTrue(doctorPigDao.findPigByFarmIdAndPigCodeAndSex(basic.getFarmId(), inputDto.getPigCode(), inputDto.getPigType()) == null, "pigCode.have.existed", inputDto.getPigCode());
    }


    @Override
    public void handle(List<DoctorEventInfo> doctorEventInfoList, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorFarmEntryDto farmEntryDto = (DoctorFarmEntryDto) inputDto;

        //1.创建猪
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

        //4.创建镜像
        DoctorPigTrack pigSnapshotTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorPigEvent pigSnapshotEvent = doctorPigEventDao.queryLastPigEventById(inputDto.getPigId());
        DoctorPigSnapshot doctorPigSnapshot = createPigSnapshot(pigSnapshotTrack, pigSnapshotEvent, doctorPigEvent.getId());
        doctorPigSnapshotDao.create(doctorPigSnapshot);

        //5.特殊处理
        specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);

        //6.记录发生的事件信息
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
                .build();
        doctorEventInfoList.add(doctorEventInfo);
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
        DoctorBarn doctorBarn = doctorBarnDao.findById(dto.getBarnId());
        DoctorPigTrack doctorPigTrack = DoctorPigTrack.builder().farmId(basic.getFarmId()).pigType(inputDto.getPigType())
                .isRemoval(IsOrNot.NO.getValue()).currentMatingCount(0)
                .pigId(inputDto.getPigId()).pigType(inputDto.getPigType())
                .currentBarnId(dto.getBarnId()).currentBarnName(dto.getBarnName())
                .currentBarnType(doctorBarn.getPigType()).currentParity(dto.getParity())
                .weight(dto.getWeight())
                .creatorId(basic.getStaffId()).creatorName(basic.getStaffName())
                .build();
        if (Objects.equals(inputDto.getPigType(), DoctorPig.PigSex.SOW.getKey())) {
            doctorPigTrack.setStatus(PigStatus.Entry.getKey());
        } else if (Objects.equals(inputDto.getPigType(), DoctorPig.PigSex.BOAR.getKey())) {
            doctorPigTrack.setStatus(PigStatus.BOAR_ENTRY.getKey());
        } else {
            throw new InvalidException("pig.sex.error", inputDto.getPigType(),inputDto.getPigCode());
        }
        //添加进场到配种标志位
        doctorPigTrack.addAllExtraMap(MapBuilder.<String, Object>of().put("enterToMate", true).map());
        return doctorPigTrack;
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
            throw new ServiceException("input.farmIdPigCode.empty");
        }

        DoctorPig doctorPig = DoctorPig.builder()
                .farmId(basic.getFarmId())
                .farmName(basic.getFarmName())
                .orgId(basic.getOrgId())
                .orgName(basic.getOrgName())
                .pigCode(dto.getPigCode())
                .pigType(dto.getPigType())
                .isRemoval(IsOrNot.NO.getValue())
                .pigFatherCode(dto.getFatherCode())
                .pigMotherCode(dto.getMotherCode())
                .source(dto.getSource())
                .birthDate(generateEventAt(dto.getBirthday()))
                .inFarmDate(generateEventAt(dto.getInFarmDate()))
                .inFarmDayAge(Years.yearsBetween(new DateTime(dto.getBirthday()), DateTime.now()).getYears())
                .initBarnId(dto.getBarnId())
                .initBarnName(dto.getBarnName())
                .breedId(dto.getBreed())
                .breedName(dto.getBreedName())
                .geneticId(dto.getBreedType())
                .geneticName(dto.getBreedTypeName())
                .boarType(dto.getBoarType())
                .remark(dto.getEntryMark())
                .creatorId(basic.getStaffId())
                .creatorName(basic.getStaffName())
                .build();
        if (Objects.equals(dto.getPigType(), DoctorPig.PigSex.SOW.getKey())) {
            // add sow pig info
            Map<String, Object> extraMapInfo = Maps.newHashMap();
            extraMapInfo.put(DoctorFarmEntryConstants.EAR_CODE, dto.getEarCode());
            extraMapInfo.put(DoctorFarmEntryConstants.FIRST_PARITY, dto.getParity());
            extraMapInfo.put(DoctorFarmEntryConstants.LEFT_COUNT, dto.getLeft());
            extraMapInfo.put(DoctorFarmEntryConstants.RIGHT_COUNT, dto.getRight());
            doctorPig.setExtraMap(extraMapInfo);
        }
        return doctorPig;
    }

}
