package io.terminus.doctor.event.reportBi.synchronizer;


import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportReserveDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.doctor.event.reportBi.helper.FieldHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.event.reportBi.helper.DateHelper.dateCN;
import static io.terminus.doctor.event.reportBi.helper.DateHelper.withDateStartDay;
import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/11.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReserveSynchronizer {

    private final DoctorPigDailyDao doctorPigDailyDao;
    private final DoctorGroupDailyDao doctorGroupDailyDao;
    private final DoctorReportReserveDao doctorReportReserveDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorReserveSynchronizer(DoctorPigDailyDao doctorPigDailyDao, DoctorGroupDailyDao doctorGroupDailyDao, DoctorReportReserveDao doctorReportReserveDao, FieldHelper fieldHelper) {
        this.doctorPigDailyDao = doctorPigDailyDao;
        this.doctorGroupDailyDao = doctorGroupDailyDao;
        this.doctorReportReserveDao = doctorReportReserveDao;
        this.fieldHelper = fieldHelper;
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



    public DoctorReportReserve buildRealTimeMaterial() {
        return null;

    }

    public DoctorReportReserve buildRealTimeMating() {
        return null;

    }

    public DoctorReportReserve buildRealTimeNursery() {
        return null;

    }

    public void synchronize(DoctorGroupDailyExtend groupDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportReserve reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportReserveDao.findByDimension(dimensionCriteria))) {
                OrzDimension orzDimension = expectNotNull(OrzDimension.from(dimensionCriteria.getOrzType()), "orzType.is.illegal");
                DateDimension dateDimension = expectNotNull(DateDimension.from(dimensionCriteria.getDateType()), "dateType.is.illegal");
                reportBI= new DoctorReportReserve();
                reportBI.setOrzType(orzDimension.getName());
                reportBI.setDateType(dateDimension.getName());
        }
        insertOrUpdate(build(groupDaily, reportBI));
    }

    public void synchronizeRealTime(DoctorGroupDailyExtend groupDaily,
                            DoctorReportReserve reportBi){
        insertOrUpdate(buildRealTime(groupDaily, reportBi));
    }

    private DoctorReportReserve build(DoctorGroupDailyExtend groupDaily,
                                      DoctorReportReserve reportBi) {


        if (Objects.equals(reportBi.getOrzType(), OrzDimension.FARM.getName())) {
            reportBi.setOrzId(groupDaily.getFarmId());
            reportBi.setOrzName(groupDaily.getFarmName());
        } else {
            reportBi.setOrzId(groupDaily.getOrgId());
            reportBi.setOrzName(groupDaily.getOrgName());
        }
        DateDimension dateDimension = DateDimension.from(reportBi.getDateType());
        reportBi.setSumAt(withDateStartDay(groupDaily.getSumAt(), dateDimension));
        reportBi.setSumAtName(dateCN(groupDaily.getSumAt(), dateDimension));
        buildRealTime(groupDaily, reportBi);
        buildDelay(groupDaily, reportBi);
        return reportBi;
    }

    private DoctorReportReserve buildRealTime(DoctorGroupDailyExtend groupDaily,
                                              DoctorReportReserve reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillGroupFiledUrl(filedUrlCriteria, groupDaily, reportBi.getOrzType(), reportBi.getDateType());
        reportBi.setStart(groupDaily.getStart());
        reportBi.setTurnInto(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getTurnInto(), "turnInto"));
        reportBi.setTurnSeed(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getTurnSeed(), "turnSeed"));
        reportBi.setDead(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getDead(), "dead"));
        reportBi.setWeedOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getWeedOut(), "weedOut"));
        reportBi.setToFatten(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToFatten(), "toFatten"));
        reportBi.setSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "sale"));
        reportBi.setChgFarmOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getChgFarm(), "chgFarm"));
        reportBi.setOtherChange(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getOtherChange(), "otherChange"));
        reportBi.setEnd(groupDaily.getEnd());
        return reportBi;
    }

    private DoctorReportReserve buildDelay(DoctorGroupDailyExtend groupDaily,
                                           DoctorReportReserve reportBi) {
        reportBi.setDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily, reportBi.getOrzType()));
        reportBi.setDailyLivestockOnHand(groupDaily.getDailyLivestockOnHand());
        return reportBi;
    }

    private void insertOrUpdate(DoctorReportReserve reserve){
        if (isNull(reserve.getId())) {
            doctorReportReserveDao.create(reserve);
            return;
        }
        doctorReportReserveDao.update(reserve);
    }

    public void deleteAll() {
        doctorReportReserveDao.deleteAll();
    }
}
