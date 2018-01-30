package io.terminus.doctor.event.reportBi;

import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiManager {
    private final DoctorReportBiDataSynchronize synchronize;
    private final DoctorReportBiDataQuery query;

    @Autowired
    public DoctorReportBiManager(DoctorReportBiDataSynchronize synchronize, DoctorReportBiDataQuery query) {
        this.synchronize = synchronize;
        this.query = query;
    }

    public void synchronizeDeltaDayBiData() {
        synchronize.synchronizeDeltaDayBiData();
    }

    public void synchronizeDeltaDayBiData(Long farmId, Date start, Integer orzType) {
        synchronize.synchronizeDeltaDayBiData(farmId, start, orzType);
    }
    /**
     * 全量同步数据到bi(手动)
     */
    public void synchronizeFullData() {
        synchronize.synchronizeFullBiData();
    }


    public DoctorDimensionReport dimensionReport(DoctorDimensionCriteria dimensionCriteria) {
        return query.dimensionReport(dimensionCriteria);
    }
 }
