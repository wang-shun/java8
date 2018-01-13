package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.event.dao.reportBi.DoctorReportFattenDao;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/13.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorFattenSynchronizer {
    private final DoctorReportFattenDao doctorReportFattenDao;

    public DoctorFattenSynchronizer(DoctorReportFattenDao doctorReportFattenDao) {
        this.doctorReportFattenDao = doctorReportFattenDao;
    }
}
