package io.terminus.doctor.event.handler.usual;

import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgFarmInEventHandler;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.terminus.doctor.event.editHandler.pig.DoctorModifyPigRemoveEventHandler.getStatus;

/**
 * Created by xjn on 17/4/27.
 * 转场转入
 */
@Component
public class DoctorChgFarmInHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorModifyPigChgFarmInEventHandler modifyPigChgFarmInEventHandler;

    public void handle(DoctorPigEvent executeEvent, DoctorPigTrack oldTrack, DoctorPig oldPig) {
        DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        DoctorPig newPig = BeanMapper.map(oldPig, DoctorPig.class);
        DoctorBarn toBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());

        //1.新建pig
        newPig.setFarmId(chgFarmDto.getToFarmId());
        newPig.setFarmName(chgFarmDto.getToFarmName());
        newPig.setIsRemoval(IsOrNot.NO.getValue());
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
        newTrack.setPigId(newPig.getId());
        newTrack.setStatus(getStatus(beforeStatusEvent));
        newTrack.setIsRemoval(IsOrNot.NO.getValue());
        newTrack.setCurrentBarnId(toBarn.getId());
        newTrack.setCurrentBarnName(toBarn.getName());
        newTrack.setCurrentBarnType(toBarn.getPigType());
        doctorPigTrackDao.create(newTrack);

        //4.更新日记录
            modifyPigChgFarmInEventHandler.updateDailyOfNew(chgFarmIn, chgFarmDto);
    }
}