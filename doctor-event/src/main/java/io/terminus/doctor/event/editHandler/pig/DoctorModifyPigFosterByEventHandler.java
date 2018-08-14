package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;

/**
 * Created by xjn on 17/4/19.
 * 被拼窝
 */
@Component
public class DoctorModifyPigFosterByEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Override
    protected void modifyHandleCheck(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        super.modifyHandleCheck(oldPigEvent, inputDto);

        DoctorFosterByDto fosterByDto = (DoctorFosterByDto) inputDto;
        //被拼窝后有断奶事件,不允许编辑被拼窝的活仔数
        if (!Objects.equals(fosterByDto.getFosterByCount(), oldPigEvent.getQuantity())) {
            notHasWean(oldPigEvent);

            //校验哺乳数量不小于零
            DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(oldPigEvent.getPigId());
            Integer unweanQty = EventUtil.plusInt(pigTrack.getUnweanQty(),
                    EventUtil.minusInt(fosterByDto.getFosterByCount(), oldPigEvent.getQuantity()));
            expectTrue(unweanQty >= 0, "modify.count.lead.to.unwean.lower.zero");
        }
    }

    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        DoctorPigEvent lastEvent = doctorPigEventDao.findLastEventExcludeTypes(deletePigEvent.getPigId(), IGNORE_EVENT);
        return Objects.equals(deletePigEvent.getId(), lastEvent.getId());
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorFosterByDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFosterByDto.class);
        DoctorFosterByDto newDto = (DoctorFosterByDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldDto.eventAt())
                .newEventAt(newDto.eventAt())
                .quantityChange(EventUtil.minusInt(newDto.getFosterByCount(), oldDto.getFosterByCount()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFosterByDto newDto = (DoctorFosterByDto) inputDto;
        newEvent.setWeight(newDto.getFosterByTotalWeight());
        newEvent.setQuantity(newDto.getFosterByCount());
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        if (Objects.equals(oldPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            oldPigTrack.setUnweanQty(EventUtil.plusInt(oldPigTrack.getUnweanQty(), changeDto.getQuantityChange()));
        }
        return oldPigTrack;
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setStatus(deletePigEvent.getPigStatusBefore());
        oldPigTrack.setUnweanQty(EventUtil.minusInt(oldPigTrack.getUnweanQty(), deletePigEvent.getQuantity()));
        if (Objects.equals(oldPigTrack.getStatus(), PigStatus.Wean.getKey())) {
            oldPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            oldPigTrack.setFarrowAvgWeight(0D);
            oldPigTrack.setFarrowQty(0);  //分娩数 0
            oldPigTrack.setWeanAvgWeight(0D);
        }
        return oldPigTrack;
    }
}
