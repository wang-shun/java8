package io.terminus.doctor.event.reportBi.factory;

import io.terminus.doctor.event.dao.reportBi.DoctorReportBoarDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportDeliverDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportEfficiencyDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportFattenDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMaterialDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMatingDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportNurseryDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportReserveDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportSowDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorReportReserve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/11.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiDataFactory {
    private final DoctorReportBoarDao doctorReportBoarDao;
    private final DoctorReportDeliverDao doctorReportDeliverDao;
    private final DoctorReportEfficiencyDao doctorReportEfficiencyDao;
    private final DoctorReportFattenDao doctorReportFattenDao;
    private final DoctorReportMaterialDao doctorReportMaterialDao;
    private final DoctorReportMatingDao doctorReportMatingDao;
    private final DoctorReportNurseryDao doctorReportNurseryDao;
    private final DoctorReportReserveDao doctorReportReserveDao;
    private final DoctorReportSowDao doctorReportSowDao;

    @Autowired
    public DoctorReportBiDataFactory(DoctorReportBoarDao doctorReportBoarDao,
                                     DoctorReportDeliverDao doctorReportDeliverDao,
                                     DoctorReportEfficiencyDao doctorReportEfficiencyDao,
                                     DoctorReportFattenDao doctorReportFattenDao,
                                     DoctorReportMaterialDao doctorReportMaterialDao,
                                     DoctorReportMatingDao doctorReportMatingDao,
                                     DoctorReportNurseryDao doctorReportNurseryDao,
                                     DoctorReportReserveDao doctorReportReserveDao,
                                     DoctorReportSowDao doctorReportSowDao) {
        this.doctorReportBoarDao = doctorReportBoarDao;
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.doctorReportEfficiencyDao = doctorReportEfficiencyDao;
        this.doctorReportFattenDao = doctorReportFattenDao;
        this.doctorReportMaterialDao = doctorReportMaterialDao;
        this.doctorReportMatingDao = doctorReportMatingDao;
        this.doctorReportNurseryDao = doctorReportNurseryDao;
        this.doctorReportReserveDao = doctorReportReserveDao;
        this.doctorReportSowDao = doctorReportSowDao;
    }

    public DoctorReportReserve buildRealTimeBoar() {
        return null;
    }

    public DoctorReportReserve buildDelayBoar(DoctorDimensionCriteria criteria) {
        return null;
    }

    public DoctorReportReserve buildRealTimeDeliver() {
        return null;

    }

    public DoctorReportReserve buildRealTimeEfficiency() {
        return null;

    }

    public DoctorReportReserve buildRealTimeFatten() {
        return null;

    }

    public DoctorReportReserve buildRealTimeMaterial() {
        return null;

    }

    public DoctorReportReserve buildRealTimeMating() {
        return null;

    }

    public DoctorReportReserve buildRealTimeNursery() {
        return null;

    }

    public DoctorReportReserve buildReserve(DoctorGroupDaily groupDaily,
                                            DoctorReportReserve reportReserve) {

        buildRealTimeReserve(groupDaily, reportReserve);
        buildDelayReserve(groupDaily, reportReserve);
        return reportReserve;
    }

    public DoctorReportReserve buildRealTimeReserve(DoctorGroupDaily groupDaily,
                                                    DoctorReportReserve reportReserve) {
        reportReserve.setStart(100);
        return reportReserve;
    }

    public DoctorReportReserve buildDelayReserve(DoctorGroupDaily groupDaily,
                                                 DoctorReportReserve reportReserve) {
        reportReserve.setDeadWeedOutRate(10D);
        return reportReserve;
    }

    public DoctorReportReserve buildRealTimeSow() {
        return null;
    }
}
