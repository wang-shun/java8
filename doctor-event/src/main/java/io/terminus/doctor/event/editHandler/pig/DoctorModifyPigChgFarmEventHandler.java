package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/19.
 * 转场
 */
@Component
public class DoctorModifyPigChgFarmEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        return false;
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorChgFarmDto newDto = (DoctorChgFarmDto) inputDto;
        DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), newDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .pigSex(newPigEvent.getKind())
                .removeCountChange(1)
                .barnType(newPigEvent.getBarnType())
                .build();
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));
        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            doctorDailyPigDao.updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(newDto.eventAt()), -changeDto2.getRemoveCountChange());
        } else {
            doctorDailyPigDao.updateDailyBoarPigLiveStock(newPigEvent.getFarmId(), getAfterDay(newDto.eventAt()), -changeDto2.getRemoveCountChange());
        }
    }

    @Override
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        //公猪
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.BOAR.getKey())) {
            oldDailyPig.setBoarIn(EventUtil.minusInt(oldDailyPig.getBoarIn(), changeDto.getRemoveCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.minusInt(oldDailyPig.getBoarEnd(), changeDto.getRemoveCountChange()));
           return oldDailyPig;
        }

        //母猪
        if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            oldDailyPig.setSowCf(EventUtil.minusInt(oldDailyPig.getSowCf(), changeDto.getRemoveCountChange()));
        } else {
            oldDailyPig.setSowPh(EventUtil.minusInt(oldDailyPig.getSowPh(), changeDto.getRemoveCountChange()));
        }
        oldDailyPig.setSowIn(EventUtil.minusInt(oldDailyPig.getSowIn(), changeDto.getRemoveCountChange()));
        oldDailyPig.setSowEnd(EventUtil.minusInt(oldDailyPig.getSowEnd(), changeDto.getRemoveCountChange()));
        return oldDailyPig;
    }
}
