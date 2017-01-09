package io.terminus.doctor.event.handler.usual;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
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
public class DoctorRemovalHandler extends DoctorAbstractEventHandler {

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorRemovalDto removalDto = (DoctorRemovalDto) inputDto;
       doctorPigTrack.setGroupId(-1L);
        doctorPigTrack.addAllExtraMap(removalDto.toMap());
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        if (Objects.equals(DoctorPig.PIG_TYPE.BOAR.getKey(), removalDto.getPigType())) {
            doctorPigTrack.setStatus(PigStatus.BOAR_LEAVE.getKey());
        } else if (Objects.equals(DoctorPig.PIG_TYPE.SOW.getKey(), removalDto.getPigType())) {
            doctorPigTrack.setStatus(PigStatus.Removal.getKey());
        } else {
            throw new IllegalStateException("basic.pigTypeValue.error");
        }
        doctorPigTrack.setIsRemoval(IsOrNot.YES.getValue());
        return doctorPigTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        super.specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);
       // 离场 事件 修改Pig 状态信息
        DoctorPig doctorPig = doctorPigDao.findById(inputDto.getPigId());
        checkState(!isNull(doctorPig), "input.doctorPigId.error");
        checkState(doctorPigDao.removalPig(doctorPig.getId()), "update.pigRemoval.fail");
    }

    @Override
    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorRemovalDto removel = (DoctorRemovalDto) inputDto;
        if (removel == null) {
            throw new ServiceException("removel.not.empty");
        }
        doctorPigEvent.setChangeTypeId(removel.getChgTypeId());   //变动类型id
        doctorPigEvent.setPrice(removel.getPrice());      //销售单价(分)
        doctorPigEvent.setAmount(removel.getSum());       //销售总额(分)

        if (Objects.equals(removel.getChgTypeId(), DoctorBasicEnums.DEAD.getId()) || Objects.equals(removel.getChgTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
            //如果是死亡 或者淘汰
            //查找最近一次配种事件
            DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());

            DateTime mattingDate = new DateTime(lastMate.getEventAt());
            DateTime eventTime = new DateTime(doctorPigEvent.getEventAt());

            int npd = Math.abs(Days.daysBetween(eventTime, mattingDate).getDays());
            if (Objects.equals(removel.getChgTypeId(), DoctorBasicEnums.DEAD.getId())) {
                //如果是死亡
                doctorPigEvent.setPsnpd(doctorPigEvent.getPsnpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            }

            if (Objects.equals(removel.getChgTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
                //如果是淘汰
                doctorPigEvent.setPtnpd(doctorPigEvent.getPtnpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            }
        }
        return doctorPigEvent;
    }
}
