package io.terminus.doctor.event.listener;

import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiDataHandler {
    private final DoctorPigDailyDao doctorPigDailyDao;
    private final DoctorGroupDailyDao doctorGroupDailyDao;

    public DoctorReportBiDataHandler(DoctorPigDailyDao doctorPigDailyDao, DoctorGroupDailyDao doctorGroupDailyDao) {
        this.doctorPigDailyDao = doctorPigDailyDao;
        this.doctorGroupDailyDao = doctorGroupDailyDao;
    }

    public void handleReverseBiData(DoctorReportBiEvent biEvent){

    }

    public void handleFullBiData(){

    }
}
