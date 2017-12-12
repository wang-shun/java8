package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dao.DoctorGroupStatisticDao;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorDailyReportV2Manager {
    private final DoctorGroupStatisticDao groupStatisticDao;

    @Autowired
    public DoctorDailyReportV2Manager(DoctorGroupStatisticDao groupStatisticDao) {
        this.groupStatisticDao = groupStatisticDao;
    }

    public void flushGroupDaily(DoctorStatisticCriteria criteria){

    }

    public void flushPigDaily(DoctorStatisticCriteria criteria) {
        // TODO: 17/12/12
    }
}
