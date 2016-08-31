package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Created by chenzenghui on 16/8/31.
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/move/data/event")
public class EventController {

    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public EventController (DoctorPigEventDao doctorPigEventDao,
                            DoctorGroupEventDao doctorGroupEventDao) {
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @RequestMapping(value = "/refreshDesc", method = RequestMethod.GET)
    public String refreshDesc(){
        Date beginDate = DateUtil.toDate("2016-08-15");
        DoctorBasicInputInfoDto basicPigInput = new DoctorBasicInputInfoDto();
        try{
            List<DoctorPigEvent> pigEvents = doctorPigEventDao.findByDateRange(beginDate, new Date());
            pigEvents.forEach(pigEvent -> {
                basicPigInput.setEventType(pigEvent.getType());
                pigEvent.setDesc(basicPigInput.generateEventDescFromExtra(pigEvent.getExtraMap()));
                doctorPigEventDao.update(pigEvent);
            });

            List<DoctorGroupEvent> groupEvents= doctorGroupEventDao.findByDateRange(beginDate, new Date());
            groupEvents.forEach(groupEvent -> {
                BaseGroupInput baseGroupInput = BaseGroupInput.generateBaseGroupInputFromTypeAndExtra(groupEvent.getExtraData(), GroupEventType.from(groupEvent.getType()));
                baseGroupInput.setIsAuto(groupEvent.getIsAuto());
                groupEvent.setDesc(baseGroupInput.generateEventDesc());
                doctorGroupEventDao.update(groupEvent);
            });
            return "ok";
        }catch(Exception e) {
            log.error("refreshDesc failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Throwables.getStackTraceAsString(e);
        }
    }

}
