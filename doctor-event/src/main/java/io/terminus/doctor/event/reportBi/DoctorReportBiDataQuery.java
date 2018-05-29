package io.terminus.doctor.event.reportBi;

import io.terminus.doctor.common.enums.IsOrNot;
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
import io.terminus.doctor.event.model.DoctorReportBoar;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import io.terminus.doctor.event.model.DoctorReportFatten;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.doctor.event.model.DoctorReportSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

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
        List<DoctorReportSow> reports = doctorReportSowDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportSow sum = doctorReportSowDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportSow();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportBoar> findBoarReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportBoar> reports = doctorReportBoarDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportBoar sum = doctorReportBoarDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportBoar();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportDeliver> findDeliverReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportDeliver> reports = doctorReportDeliverDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportDeliver sum = doctorReportDeliverDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportDeliver();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportEfficiency> findEfficiencyReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportEfficiency> reports = doctorReportEfficiencyDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportEfficiency sum = new DoctorReportEfficiency();
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportFatten> findFattenReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportFatten> reports = doctorReportFattenDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportFatten sum = doctorReportFattenDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportFatten();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportMating> findMatingReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportMating> reports = doctorReportMatingDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportMating sum = doctorReportMatingDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportMating();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportNursery> findNurseryReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportNursery> reports = doctorReportNurseryDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportNursery sum = doctorReportNurseryDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportNursery();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportReserve> findReserveReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportReserve> reports = doctorReportReserveDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportReserve sum = doctorReportReserveDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportReserve();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }

    public List<DoctorReportMaterial> findMaterialReportBy(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportMaterial> reports = doctorReportMaterialDao.findBy(dimensionCriteria);
        if (Objects.equals(dimensionCriteria.getIsNecessaryTotal(), IsOrNot.YES.getKey())) {
            DoctorReportMaterial sum = doctorReportMaterialDao.sumBy(dimensionCriteria);
            if (isNull(sum)) {
                sum = new DoctorReportMaterial();
            }
            sum.setId(-1L);
            reports.add(sum);
        }
        return reports;
    }
}
