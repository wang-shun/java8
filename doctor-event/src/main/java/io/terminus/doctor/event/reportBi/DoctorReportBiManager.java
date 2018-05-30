package io.terminus.doctor.event.reportBi;

import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

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

    /**
     * 增量同步，每个猪场指定时间后的数据
     * @param longDateMap 猪场id与同步日期的map
     */
    public void synchronizeDeltaDayBiData(Map<Long, Date> longDateMap) {
        synchronize.synchronizeDeltaDayBiData(longDateMap);
    }

    /**
     * 同步某组织维度指定时间后的数据
     * @param orzId
     * @param start
     * @param orzType
     */
    public void synchronizeDeltaDayBiData(Long orzId, Date start, Integer orzType) {
        synchronize.synchronizeDeltaDayBiData(orzId, start, orzType);
    }

    public void synchronizeDelta(Long farmId, Date start, Integer orzType) {
        synchronize.synchronizeDelta(farmId, orzType, start, 1);
    }

    /**
     * 全量同步数据到bi(手动)
     */
    public void synchronizeFullData() {
        synchronize.synchronizeFullBiData();
    }


    /**
     * 查询某一维度条件下的报表数据
     * @param dimensionCriteria 条件
     * @return
     */
    public DoctorDimensionReport dimensionReport(DoctorDimensionCriteria dimensionCriteria) {
        return query.dimensionReport(dimensionCriteria);
    }
 }
