package io.terminus.doctor.event.reportBi.listener;

import io.terminus.doctor.event.cache.DoctorDepartmentCache;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiDataSynchronizer {
    private final DoctorPigDailyDao doctorPigDailyDao;
    private final DoctorGroupDailyDao doctorGroupDailyDao;
    private final DoctorDepartmentCache departmentCache;

    public DoctorReportBiDataSynchronizer(DoctorPigDailyDao doctorPigDailyDao,
                                          DoctorGroupDailyDao doctorGroupDailyDao, DoctorDepartmentCache departmentCache) {
        this.doctorPigDailyDao = doctorPigDailyDao;
        this.doctorGroupDailyDao = doctorGroupDailyDao;
        this.departmentCache = departmentCache;
    }

    /**
     * 全量同步数据
     */
    public void synchronizeFullBiData(){

    }

    /**
     * 增量同步数据
     */
    public void synchronizeDeltaBiData(){

    }

    /**
     * 同步实时数据
     */
    public void synchronizeRealTimeBiData(){

    }

    /**
     * 同步延时数据
     */
    public void synchronizeDelayBiData(){

    }

}
