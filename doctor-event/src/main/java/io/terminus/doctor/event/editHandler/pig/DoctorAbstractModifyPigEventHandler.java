package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.editHandler.DoctorModifyPigEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 17/4/13.
 * 猪事件编辑抽象实现
 */
public abstract class DoctorAbstractModifyPigEventHandler implements DoctorModifyPigEventHandler{
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Override
    public void handleCheck(DoctorPigEvent oldPigEvent, DoctorEventChangeDto changeDto) {

    }

    @Override
    public void handle(DoctorPigEvent oldPigEvent, DoctorEventChangeDto changeDto) {
        //1.更新事件
        DoctorPigEvent newEvent = buildNewEvent(oldPigEvent, changeDto);
        doctorPigEventDao.update(newEvent);

        //2.更新猪信息
        if (isUpdatePig(changeDto)) {
            DoctorPig oldPig = doctorPigDao.findById(oldPigEvent.getId());
            DoctorPig newPig = buildNewPig(oldPig, changeDto);
            doctorPigDao.update(newPig);
        }

        //3.更新track
        if (isUpdateTrack(changeDto)) {
            DoctorPigTrack oldPigTrack = doctorPigTrackDao.findByPigId(oldPigEvent.getPigId());
            DoctorPigTrack newTrack = buildNewTrack(oldPigTrack, changeDto);
            doctorPigTrackDao.update(newTrack);
        }

        //4.更新每日数据记录

        //5.调用触发事件的编辑

    }

    protected boolean isUpdatePig(DoctorEventChangeDto changeDto){
        return false;
    }

    protected boolean isUpdateTrack(DoctorEventChangeDto changeDto){
        return false;
    }
}
