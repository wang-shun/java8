package io.terminus.doctor.event.handler.usual;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.enums.PigType.MATING_FARROW_TYPES;
import static io.terminus.doctor.common.enums.PigType.MATING_TYPES;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorChgFarmHandler extends DoctorAbstractEventHandler{

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        expectTrue(!Objects.equals(fromTrack.getStatus(), PigStatus.FEED.getKey()), "feed.sow.not.chg.farm");
        DoctorBarn doctorCurrentBarn = doctorBarnDao.findById(fromTrack.getCurrentBarnId());
        expectTrue(notNull(doctorCurrentBarn), "barn.not.null", fromTrack.getCurrentBarnId());
        DoctorBarn doctorToBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());
        expectTrue(notNull(doctorToBarn), "barn.not.null", chgFarmDto.getToBarnId());
        expectTrue(checkBarnTypeEqual(doctorCurrentBarn, doctorToBarn, fromTrack.getStatus()), "not.trans.barn.type", PigType.from(doctorCurrentBarn.getPigType()).getDesc(), PigType.from(doctorToBarn.getPigType()).getDesc());

    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        return super.buildPigEvent(basic, inputDto);
    }

    @Override
    protected void specialHandleBefore(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        DoctorPig oldPig = doctorPigDao.findById(executeEvent.getPigId());
        DoctorPig newPig = BeanMapper.map(oldPig, DoctorPig.class);
        DoctorBarn toBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());

        //1.新建一头猪
        newPig.setFarmId(chgFarmDto.getToFarmId());
        newPig.setFarmName(chgFarmDto.getToFarmName());
        doctorPigDao.create(newPig);

        //2.新建track
        DoctorPigTrack newTrack = BeanMapper.map(fromTrack, DoctorPigTrack.class);
        newTrack.setPigId(newPig.getId());
        newTrack.setCurrentBarnId(toBarn.getId());
        newTrack.setCurrentBarnName(toBarn.getName());
        newTrack.setCurrentBarnType(toBarn.getPigType());
        doctorPigTrackDao.create(newTrack);

        //3.复制之前之前更改pigId
        List<DoctorPigEvent> pigEventList = doctorPigEventDao.findByPigId(oldPig.getId());
        pigEventList.forEach(pigEvent -> {
            pigEvent.setFarmId(chgFarmDto.getToFarmId());
            pigEvent.setFarmName(chgFarmDto.getToFarmName());
            pigEvent.setPigId(newPig.getId());
            pigEvent.setEventSource(SourceType.TRANS_FARM.getValue());
        });
        doctorPigEventDao.creates(pigEventList);
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        toTrack.setStatus(PigStatus.Removal.getKey());
        toTrack.setIsRemoval(IsOrNot.YES.getValue());
        return toTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        DoctorPig oldPig = doctorPigDao.findById(executeEvent.getPigId());
        oldPig.setIsRemoval(IsOrNot.YES.getValue());
        doctorPigDao.update(oldPig);
    }

    /**
     * 校验是否可以转舍
     * @param fromBarn 源舍
     * @param toBarn 转入舍
     * @param pigStatus 状态
     * @return 是否准许转舍
     */
    private Boolean checkBarnTypeEqual(DoctorBarn fromBarn, DoctorBarn toBarn, Integer pigStatus) {
        if (fromBarn == null || toBarn == null) {
            return false;
        }
        return (Objects.equals(fromBarn.getPigType(), toBarn.getPigType())
                || (MATING_TYPES.contains(fromBarn.getPigType()) && MATING_TYPES.contains(toBarn.getPigType()))
                || (Objects.equals(fromBarn.getPigType(), PigType.DELIVER_SOW.getValue()) && MATING_FARROW_TYPES.contains(toBarn.getPigType())))
                || Objects.equals(pigStatus, PigStatus.Pregnancy.getKey()) && MATING_FARROW_TYPES.contains(toBarn.getPigType());
    }
}
