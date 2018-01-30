package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.event.dao.reportBi.DoctorReportBoarDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorPigDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportBoar;
import io.terminus.doctor.event.reportBi.helper.FieldHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.event.reportBi.helper.DateHelper.dateCN;
import static io.terminus.doctor.event.reportBi.helper.DateHelper.withDateStartDay;
import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/13.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorBoarSynchronizer {
    private final DoctorReportBoarDao doctorReportBoarDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorBoarSynchronizer(DoctorReportBoarDao doctorReportBoarDao, FieldHelper fieldHelper) {
        this.doctorReportBoarDao = doctorReportBoarDao;
        this.fieldHelper = fieldHelper;
    }

    public void synchronize(DoctorPigDailyExtend pigDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportBoar reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportBoarDao.findByDimension(dimensionCriteria))) {
            reportBI= new DoctorReportBoar();
            reportBI.setOrzType(dimensionCriteria.getOrzType());
            reportBI.setDateType(dimensionCriteria.getDateType());
        }
        insertOrUpdate(build(pigDaily, reportBI, dimensionCriteria.getIsRealTime()));
    }

    private void insertOrUpdate(DoctorReportBoar reportBi){
        if (isNull(reportBi.getId())) {
            doctorReportBoarDao.create(reportBi);
            return;
        }
        doctorReportBoarDao.update(reportBi);
    }

    public DoctorReportBoar build(DoctorPigDailyExtend pigDaily, DoctorReportBoar reportBi, Integer isRealTime) {
        if (Objects.equals(reportBi.getOrzType(), OrzDimension.FARM.getValue())) {
            reportBi.setOrzId(pigDaily.getFarmId());
            reportBi.setOrzName(pigDaily.getFarmName());
        } else {
            reportBi.setOrzId(pigDaily.getOrgId());
            reportBi.setOrzName(pigDaily.getOrgName());
        }
        DateDimension dateDimension = DateDimension.from(reportBi.getDateType());
        reportBi.setSumAt(withDateStartDay(pigDaily.getSumAt(), dateDimension));
        reportBi.setSumAtName(dateCN(pigDaily.getSumAt(), dateDimension));
        buildRealTime(pigDaily, reportBi);
        if (!Objects.equals(isRealTime, IsOrNot.YES.getKey())) {
            buildDelay(pigDaily, reportBi);
        }
        return reportBi;
    }

    private void buildRealTime(DoctorPigDailyExtend pigDaily, DoctorReportBoar reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillPigFiledUrl(filedUrlCriteria, pigDaily, reportBi.getOrzType(), reportBi.getDateType());
        reportBi.setStart(pigDaily.getBoarStart());
        reportBi.setTurnInto(turnInto(pigDaily, reportBi));
        reportBi.setDead(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getBoarDead(), "boarDead"));
        reportBi.setWeedOut(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getBoarWeedOut(), "boarWeedOut"));
        reportBi.setSale(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getBoarSale(), "boarSale"));
        reportBi.setOtherChange(pigDaily.getBoarOtherOut());
        reportBi.setEnd(pigDaily.getBoarEnd());
    }

    private void buildDelay(DoctorPigDailyExtend pigDaily, DoctorReportBoar reportBi) {
        reportBi.setDailyPigCount(pigDaily.getBoarDailyPigCount());
    }

    public Integer turnInto(DoctorPigDailyExtend dailyExtend, DoctorReportBoar reportBi) {
        if (Objects.equals(reportBi.getOrzType(), OrzDimension.ORG.getValue())) {
            return dailyExtend.getBoarIn() - dailyExtend.getBoarChgFarmIn();
        }
        return dailyExtend.getBoarIn();
    }

    public void deleteAll() {
        doctorReportBoarDao.deleteAll();
    }
}
