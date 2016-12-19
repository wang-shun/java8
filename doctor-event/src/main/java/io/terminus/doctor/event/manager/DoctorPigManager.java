package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by chenzenghui on 16/12/19.
 */
@Component
@Slf4j
public class DoctorPigManager {

    private final DoctorPigDao doctorPigDao;
    private final DoctorPigEventDao doctorPigEventDao;

    @Autowired
    public DoctorPigManager(DoctorPigDao doctorPigDao,
                            DoctorPigEventDao doctorPigEventDao) {
        this.doctorPigDao = doctorPigDao;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    @Transactional
    public boolean updatePigCode(Long pigId, String code){
        DoctorPig pig = new DoctorPig();
        pig.setId(pigId);
        pig.setPigCode(code);
        doctorPigDao.update(pig);
        doctorPigEventDao.updatePigCode(pigId, code);
        return true;
    }
}
