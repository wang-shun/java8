package io.terminus.doctor.event.handler.usual;

import com.google.common.collect.Maps;
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
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

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
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
    }

    @Override
    public void handle(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {

        //如果是编辑进场事件需要更新猪信息
        if (Objects.equals(executeEvent.getIsModify(), IsOrNot.YES.getValue())) {
            DoctorPig updatePig = doctorPigDao.findById(executeEvent.getPigId());
            doctorPigDao.update(buildUpdatePig(executeEvent, updatePig));
        }

        //1.创建事件
        doctorPigEventDao.create(executeEvent);

        //2.创建或更新track
        DoctorPigTrack toTrack = buildPigTrack(executeEvent, fromTrack);
        if (Objects.equals(executeEvent.getIsModify(), IsOrNot.YES.getValue())) {
            toTrack.setId(fromTrack.getId());
            doctorPigTrackDao.update(toTrack);
        } else {
            doctorPigTrackDao.create(toTrack);
        }
        //3.创建镜像
        createPigSnapshot(toTrack, executeEvent, 0L);

        //4.特殊处理
        specialHandle(executeEvent, toTrack);

        //5.记录发生的事件信息
        DoctorEventInfo doctorEventInfo = DoctorEventInfo.builder()
                .orgId(executeEvent.getOrgId())
                .farmId(executeEvent.getFarmId())
                .eventId(executeEvent.getId())
                .eventAt(executeEvent.getEventAt())
                .kind(executeEvent.getKind())
                .mateType(executeEvent.getDoctorMateType())
                .pregCheckResult(executeEvent.getPregCheckResult())
                .businessId(executeEvent.getPigId())
                .code(executeEvent.getPigCode())
                .status(toTrack.getStatus())
                .businessType(DoctorEventInfo.Business_Type.PIG.getValue())
                .eventType(executeEvent.getType())
                .build();
        doctorEventInfoList.add(doctorEventInfo);
    }

    /**
     * 构建DoctorPig
     *
     * @param dto 进场信息
     * @param basic 基础数据
     * @return 猪
     */
    private DoctorPig buildDoctorPig(DoctorFarmEntryDto dto, DoctorBasicInputInfoDto basic) {
        expectTrue(doctorPigDao.findPigByFarmIdAndPigCodeAndSex(basic.getFarmId(), dto.getPigCode(), dto.getPigType()) == null, "pigCode.have.existed");
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

    /**
     * 构建更新猪(修改事件使用)
     * @param executeEvent 修改的进场事件
     * @param pig 原猪信息
     * @return 更改后猪信息
     */
    private DoctorPig buildUpdatePig(DoctorPigEvent executeEvent, DoctorPig pig) {
        DoctorFarmEntryDto farmEntryDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorFarmEntryDto.class);
        pig.setBirthDate(farmEntryDto.getBirthday());
        pig.setInFarmDate(executeEvent.getEventAt());
        pig.setPigCode(executeEvent.getPigCode());
        pig.setBreedId(farmEntryDto.getBreed());
        pig.setBreedName(farmEntryDto.getBreedName());
        pig.setPigFatherCode(farmEntryDto.getFatherCode());
        pig.setPigMotherCode(farmEntryDto.getMotherCode());
        pig.setSource(farmEntryDto.getSource());
        pig.setBoarType(farmEntryDto.getBoarType());
        return pig;
    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorFarmEntryDto farmEntryDto = (DoctorFarmEntryDto) inputDto;
        DoctorPig doctorPig = buildDoctorPig(farmEntryDto, basic);
        doctorPigDao.create(doctorPig);
        farmEntryDto.setPigId(doctorPig.getId());
        DoctorPigEvent doctorPigEvent =  super.buildPigEvent(basic, inputDto);
        doctorPigEvent.setParity(farmEntryDto.getParity());
        return doctorPigEvent;
    }

    /**
     * 构建猪进厂 Track 信息表
     * @param executeEvent 执行事件
     * @param fromTrack 原状态 进场为null(非编辑时),编辑时为进场之后track
     * @return 猪track
     */
    @Override
    protected DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorFarmEntryDto farmEntryDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorFarmEntryDto.class);
        DoctorBarn doctorBarn = doctorBarnDao.findById(executeEvent.getBarnId());
        expectTrue(notNull(doctorBarn), "barn.not.null", executeEvent.getBarnId());
        DoctorPigTrack doctorPigTrack = DoctorPigTrack.builder().farmId(executeEvent.getFarmId())
                .isRemoval(IsOrNot.NO.getValue()).currentMatingCount(0)
                .pigId(executeEvent.getPigId()).pigType(executeEvent.getKind())
                .currentBarnId(doctorBarn.getId()).currentBarnName(doctorBarn.getName())
                .currentBarnType(doctorBarn.getPigType()).currentParity(executeEvent.getParity())
                .weight(farmEntryDto.getWeight())
                .creatorId(executeEvent.getOperatorId()).creatorName(executeEvent.getOperatorName())
                .currentEventId(executeEvent.getId())
                .build();
        if (Objects.equals(executeEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            doctorPigTrack.setStatus(PigStatus.Entry.getKey());
        } else if (Objects.equals(executeEvent.getKind(), DoctorPig.PigSex.BOAR.getKey())) {
            doctorPigTrack.setStatus(PigStatus.BOAR_ENTRY.getKey());
        } else {
            throw new InvalidException("pig.sex.error", executeEvent.getKind());
        }
        //添加进场到配种标志位
        doctorPigTrack.addAllExtraMap(MapBuilder.<String, Object>of().put("enterToMate", true).map());
        return doctorPigTrack;
    }


    @Override
    protected void specialHandle(DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        super.specialHandle(executeEvent, toTrack);
        doctorPigInfoCache.addPigCodeToFarm(executeEvent.getFarmId(), executeEvent.getPigCode());
    }
}
