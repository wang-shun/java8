package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Component;

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
public class DoctorRemovalHandler extends DoctorAbstractEventHandler {
    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        expectTrue(!Objects.equals(fromTrack.getStatus(), PigStatus.FEED.getKey()), "removal.status.not.feed");
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        //DoctorRemovalDto removalDto = (DoctorRemovalDto) inputDto;
        toTrack.setGroupId(-1L);
        //doctorPigTrack.addAllExtraMap(removalDto.toMap());
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        if (Objects.equals(DoctorPig.PigSex.BOAR.getKey(), toTrack.getPigType())) {
            toTrack.setStatus(PigStatus.BOAR_LEAVE.getKey());
        } else if (Objects.equals(DoctorPig.PigSex.SOW.getKey(), toTrack.getPigType())) {
            toTrack.setStatus(PigStatus.Removal.getKey());
        } else {
            throw new InvalidException("pig.sex.error", toTrack.getPigType(),executeEvent.getPigCode());
        }
        toTrack.setIsRemoval(IsOrNot.YES.getValue());
        return toTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent inputEvent, DoctorPigTrack currentTrack) {
        super.specialHandle(inputEvent, currentTrack);
       // 离场 事件 修改Pig 状态信息
        DoctorPig doctorPig = doctorPigDao.findById(inputEvent.getPigId());
        expectTrue(notNull(doctorPig), "pig.not.null", inputEvent.getPigId());
        doctorPigDao.removalPig(doctorPig.getId());
    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorRemovalDto removalDto = (DoctorRemovalDto) inputDto;
        doctorPigEvent.setWeight(removalDto.getWeight());
        doctorPigEvent.setCustomerId(removalDto.getCustomerId());
        doctorPigEvent.setCustomerName(removalDto.getCustomerName());

        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        expectTrue(notNull(doctorPigTrack), "pig.track.not.null", inputDto.getPigId());

        doctorPigEvent.setChangeTypeId(removalDto.getChgTypeId());   //变动类型id
        doctorPigEvent.setPrice(removalDto.getPrice());      //销售单价(分)

        if (removalDto.getWeight() != null && removalDto.getPrice() != null) {
            doctorPigEvent.setAmount((long) (removalDto.getPrice() * removalDto.getWeight()));       //销售总额(分)
        }

        if (Objects.equals(removalDto.getChgTypeId(), DoctorBasicEnums.DEAD.getId()) || Objects.equals(removalDto.getChgTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
            //如果是死亡 或者淘汰,查找最近一次配种事件
            DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
            if (lastMate == null) {
                return doctorPigEvent;
            }
            DateTime mattingDate = new DateTime(lastMate.getEventAt());
            DateTime eventTime = new DateTime(doctorPigEvent.getEventAt());

            int npd = Math.abs(Days.daysBetween(eventTime, mattingDate).getDays());
            if (Objects.equals(removalDto.getChgTypeId(), DoctorBasicEnums.DEAD.getId())) {
                //如果是死亡
                doctorPigEvent.setPsnpd(doctorPigEvent.getPsnpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            }
            if (Objects.equals(removalDto.getChgTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
                //如果是淘汰
                doctorPigEvent.setPtnpd(doctorPigEvent.getPtnpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            }
        }
        return doctorPigEvent;
    }
}
