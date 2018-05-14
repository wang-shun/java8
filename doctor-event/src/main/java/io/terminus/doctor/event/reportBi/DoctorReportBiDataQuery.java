package io.terminus.doctor.event.reportBi;

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
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;
import io.terminus.doctor.event.model.DoctorReportSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xjn on 18/1/18.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiDataQuery {

    private final DoctorReportBoarDao doctorReportBoarDao;
    private final DoctorReportSowDao doctorReportSowDao;
    private final DoctorReportReserveDao doctorReportReserveDao;
    private final DoctorReportNurseryDao doctorReportNurseryDao;
    private final DoctorReportFattenDao doctorReportFattenDao;
    private final DoctorReportDeliverDao doctorReportDeliverDao;
    private final DoctorReportMatingDao doctorReportMatingDao;
    private final DoctorReportMaterialDao doctorReportMaterialDao;
    private final DoctorReportEfficiencyDao doctorReportEfficiencyDao;

    @Autowired
    public DoctorReportBiDataQuery(DoctorReportBoarDao doctorReportBoarDao,
                                   DoctorReportSowDao doctorReportSowDao,
                                   DoctorReportReserveDao doctorReportReserveDao,
                                   DoctorReportNurseryDao doctorReportNurseryDao,
                                   DoctorReportFattenDao doctorReportFattenDao,
                                   DoctorReportDeliverDao doctorReportDeliverDao,
                                   DoctorReportMatingDao doctorReportMatingDao,
                                   DoctorReportMaterialDao doctorReportMaterialDao,
                                   DoctorReportEfficiencyDao doctorReportEfficiencyDao) {
        this.doctorReportBoarDao = doctorReportBoarDao;
        this.doctorReportSowDao = doctorReportSowDao;
        this.doctorReportReserveDao = doctorReportReserveDao;
        this.doctorReportNurseryDao = doctorReportNurseryDao;
        this.doctorReportFattenDao = doctorReportFattenDao;
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.doctorReportMatingDao = doctorReportMatingDao;
        this.doctorReportMaterialDao = doctorReportMaterialDao;
        this.doctorReportEfficiencyDao = doctorReportEfficiencyDao;
    }

    public DoctorDimensionReport dimensionReport(DoctorDimensionCriteria dimensionCriteria) {
        DoctorDimensionReport dimensionReport = new DoctorDimensionReport();
        dimensionReport.setReportBoar(doctorReportBoarDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportDeliver(doctorReportDeliverDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportEfficiency(doctorReportEfficiencyDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportFatten(doctorReportFattenDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportMaterial(doctorReportMaterialDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportMating(doctorReportMatingDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportNursery(doctorReportNurseryDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportReserve(doctorReportReserveDao.findByDimension(dimensionCriteria));
        dimensionReport.setReportSow(doctorReportSowDao.findByDimension(dimensionCriteria));
        return dimensionReport;
    }

    public List<DoctorReportSow> findSowReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportSow> reportSows = doctorReportSowDao.findBy(dimensionCriteria);
        reportSows.add(doctorReportSowDao.sumBy(dimensionCriteria));
        return reportSows;
    }
}
