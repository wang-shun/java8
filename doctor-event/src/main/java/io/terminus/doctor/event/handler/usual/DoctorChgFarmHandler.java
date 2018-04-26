package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgFarmEventHandler;
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
import java.util.stream.Collectors;

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
@Deprecated
@Component
@Slf4j
public class DoctorChgFarmHandler extends DoctorAbstractEventHandler{

    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorModifyPigChgFarmEventHandler modifyPigChgFarmEventHandler;
    @Autowired
    private DoctorChgFarmInHandler doctorChgFarmInHandler;

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        expectTrue(!Objects.equals(fromTrack.getStatus(), PigStatus.FEED.getKey()), "feed.sow.not.chg.farm");
        DoctorBarn doctorCurrentBarn = doctorBarnDao.findById(fromTrack.getCurrentBarnId());
        expectTrue(notNull(doctorCurrentBarn), "barn.not.null", fromTrack.getCurrentBarnId());
        DoctorBarn doctorToBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());
        expectTrue(notNull(doctorToBarn), "barn.not.null", chgFarmDto.getToBarnId());
        List<Long> barns = doctorBarnDao.findByFarmId(chgFarmDto.getToFarmId())
                .stream().map(DoctorBarn::getId).collect(Collectors.toList());
        expectTrue(barns.contains(doctorToBarn.getId()), "toBarn.not.in.toFarm");
        expectTrue(checkBarnTypeEqual(doctorCurrentBarn, doctorToBarn, fromTrack.getStatus()), "not.trans.barn.type",
                PigType.from(doctorCurrentBarn.getPigType()).getDesc(), PigType.from(doctorToBarn.getPigType()).getDesc());

    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        return super.buildPigEvent(basic, inputDto);
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        if (Objects.equals(executeEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            toTrack.setStatus(PigStatus.Removal.getKey());
        } else {
            toTrack.setStatus(PigStatus.BOAR_LEAVE.getKey());
        }
        toTrack.setIsRemoval(IsOrNot.YES.getValue());
        return toTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        super.specialHandle(executeEvent, toTrack);
        DoctorPig oldPig = doctorPigDao.findById(executeEvent.getPigId());
        oldPig.setIsRemoval(IsOrNot.YES.getValue());
        doctorPigDao.update(oldPig);
    }

    @Override
    protected void updateDailyForNew(DoctorPigEvent newPigEvent) {
        BasePigEventInputDto inputDto = JSON_MAPPER.fromJson(newPigEvent.getExtra(), DoctorChgFarmDto.class);
        modifyPigChgFarmEventHandler.updateDailyOfNew(newPigEvent, inputDto);
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        DoctorPig doctorPig = doctorPigDao.findById(toTrack.getPigId());
        doctorChgFarmInHandler.handle(doctorEventInfoList, executeEvent, toTrack, doctorPig);
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
