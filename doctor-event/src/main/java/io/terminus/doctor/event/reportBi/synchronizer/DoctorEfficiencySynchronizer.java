package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.event.dao.DoctorReportNpdDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sunbo@terminus.io on 2018/1/15.
 */
@Component
public class DoctorEfficiencySynchronizer {

    @Autowired
    private DoctorReportNpdDao doctorReportNpdDao;

    public void deleteAll() {
        doctorReportNpdDao.delete();
    }
}
