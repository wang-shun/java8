package io.terminus.doctor.event.handler.usual;

import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgFarmInEventHandler;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.doctor.common.enums.PigType.PREG_SOW;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.helper.DoctorEventBaseHelper.getStatus;

/**
 * Created by xjn on 17/4/27.
 * 转场转入
 */
@Component
public class DoctorChgFarmInHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorModifyPigChgFarmInEventHandler modifyPigChgFarmInEventHandler;

    public void handle(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack oldTrack, DoctorPig oldPig) {
        if (isNull(executeEvent.getEventSource())
                || Objects.equals(executeEvent.getEventSource(), SourceType.INPUT.getValue())) {
            String key = executeEvent.getFarmId().toString() + executeEvent.getKind().toString() + executeEvent.getPigCode();
            expectTrue(doctorConcurrentControl.setKey(key), "event.concurrent.error", executeEvent.getPigCode());
        }

        DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        DoctorPig newPig = BeanMapper.map(oldPig, DoctorPig.class);
        DoctorBarn toBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());

        //1.新建pig
        expectTrue(isNull(doctorPigDao.findPigByFarmIdAndPigCodeAndSex(chgFarmDto.getToFarmId(), oldPig.getPigCode(), oldPig.getPigType())), "pigCode.have.existed");

        newPig.setFarmId(chgFarmDto.getToFarmId());
        newPig.setFarmName(chgFarmDto.getToFarmName());
        newPig.setIsRemoval(IsOrNot.NO.getValue());
        newPig.setInFarmDate(chgFarmDto.getChgFarmDate());
        newPig.setInitBarnId(chgFarmDto.getToBarnId());
        newPig.setInitBarnName(chgFarmDto.getToBarnName());
        doctorPigDao.create(newPig);

        //3.复制之前事件
        List<DoctorPigEvent> pigEventList = doctorPigEventDao.findByPigId(oldPig.getId());
        DoctorPigEvent chgFarmIn = null;
        List<DoctorPigEvent> pigEvents = Lists.newArrayList();
        for (DoctorPigEvent pigEvent : pigEventList) {
            pigEvent.setFarmId(chgFarmDto.getToFarmId());
            pigEvent.setFarmName(chgFarmDto.getToFarmName());
            pigEvent.setPigId(newPig.getId());
            if (Objects.equals(pigEvent.getId(), executeEvent.getId())) {
                pigEvent.setBarnId(toBarn.getId());
                pigEvent.setBarnName(toBarn.getName());
                pigEvent.setBarnType(toBarn.getPigType());
                pigEvent.setType(PigEvent.CHG_FARM_IN.getKey());
                pigEvent.setName(PigEvent.CHG_FARM_IN.getName());
                pigEvent.setIsAuto(IsOrNot.YES.getValue());
                pigEvent.setRelPigEventId(executeEvent.getId());
                chgFarmIn = pigEvent;
            } else {
                pigEvent.setEventSource(SourceType.TRANS_FARM.getValue());
                pigEvents.add(pigEvent);
            }
        }
        doctorPigEventDao.creates(pigEvents);

        DoctorPigEvent beforeStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(newPig.getId(), executeEvent.getEventAt());

        doctorPigEventDao.create(chgFarmIn);

        //2.新建track
        DoctorPigTrack newTrack = BeanMapper.map(oldTrack, DoctorPigTrack.class);
        newTrack.setFarmId(chgFarmDto.getToFarmId());
        newTrack.setPigId(newPig.getId());
        newTrack.setStatus(getStatus(beforeStatusEvent));

        DoctorBarn fromBarn = doctorBarnDao.findById(chgFarmDto.getFromBarnId());
        if (Objects.equals(fromBarn.getPigType(), PREG_SOW.getValue()) && Objects.equals(toBarn.getPigType(), PigType.DELIVER_SOW.getValue())) {
            newTrack.setStatus(PigStatus.Farrow.getKey());
        }

        newTrack.setIsRemoval(IsOrNot.NO.getValue());
        newTrack.setCurrentBarnId(toBarn.getId());
        newTrack.setCurrentBarnName(toBarn.getName());
        newTrack.setCurrentBarnType(toBarn.getPigType());

        chgFarmIn.setPigStatusAfter(newTrack.getStatus());
        doctorPigEventDao.update(chgFarmIn);

        //校验track
        doctorEventBaseHelper.validTrackAfterUpdate(newTrack);
        doctorPigTrackDao.create(newTrack);

        //记录发生的事件信息
        DoctorBarn doctorBarn = doctorBarnDao.findById(newTrack.getCurrentBarnId());
        DoctorEventInfo doctorEventInfo = DoctorEventInfo.builder()
                .orgId(chgFarmIn.getOrgId())
                .farmId(chgFarmIn.getFarmId())
                .eventId(chgFarmIn.getId())
                .isAuto(chgFarmIn.getIsAuto())
                .eventAt(chgFarmIn.getEventAt())
                .kind(chgFarmIn.getKind())
                .mateType(chgFarmIn.getDoctorMateType())
                .pregCheckResult(chgFarmIn.getPregCheckResult())
                .businessId(chgFarmIn.getPigId())
                .code(chgFarmIn.getPigCode())
                .status(newTrack.getStatus())
                .preStatus(chgFarmIn.getPigStatusBefore())
                .businessType(DoctorEventInfo.Business_Type.PIG.getValue())
                .eventType(chgFarmIn.getType())
                .pigType(doctorBarn.getPigType())
                .build();
        doctorEventInfoList.add(doctorEventInfo);

        //新增事件后记录track snapshot
        createTrackSnapshot(chgFarmIn);
        //4.更新日记录
        modifyPigChgFarmInEventHandler.updateDailyOfNew(chgFarmIn, chgFarmDto);
    }
}
