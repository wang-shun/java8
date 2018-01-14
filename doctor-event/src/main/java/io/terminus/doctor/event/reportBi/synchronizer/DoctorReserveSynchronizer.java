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
import io.terminus.doctor.event.reportBi.helper.DateHelper;
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
        insertOrUpdateReserve(buildReserve(groupDaily, reportBI));
    }

    public void synchronizeRealTime(DoctorGroupDailyExtend groupDaily,
                            DoctorReportReserve reportReserve){
        insertOrUpdateReserve(buildRealTimeReserve(groupDaily, reportReserve));
    }

    private DoctorReportReserve buildReserve(DoctorGroupDailyExtend groupDaily,
                                            DoctorReportReserve reportReserve) {


        if (Objects.equals(reportReserve.getOrzType(), OrzDimension.FARM.getName())) {
            reportReserve.setOrzId(groupDaily.getFarmId());
            reportReserve.setOrzName(groupDaily.getFarmName());
        } else {
            reportReserve.setOrzId(groupDaily.getOrgId());
            reportReserve.setOrzName(groupDaily.getOrgName());
        }
        DateDimension dateDimension = DateDimension.from(reportReserve.getDateType());
        reportReserve.setSumAt(withDateStartDay(groupDaily.getSumAt(), dateDimension));
        reportReserve.setSumAtName(dateCN(groupDaily.getSumAt(), dateDimension));
        buildRealTimeReserve(groupDaily, reportReserve);
        buildDelayReserve(groupDaily, reportReserve);
        return reportReserve;
    }

    private DoctorReportReserve buildRealTimeReserve(DoctorGroupDailyExtend groupDaily,
                                                    DoctorReportReserve reportReserve) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        filedUrlCriteria.setFarmId(groupDaily.getFarmId());
        filedUrlCriteria.setPigType(groupDaily.getPigType());
        DateDimension dateDimension = DateDimension.from(reportReserve.getDateType());
        filedUrlCriteria.setStart(DateHelper.withDateStartDay(groupDaily.getSumAt(), dateDimension));
        filedUrlCriteria.setEnd(DateHelper.withDateEndDay(groupDaily.getSumAt(), dateDimension));

        reportReserve.setStart(groupDaily.getStart());
        reportReserve.setTurnInto(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getTurnInto(), "turnInto"));
        reportReserve.setTurnSeed(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getTurnSeed(), "turnSeed"));
        reportReserve.setDead(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getDead(), "dead"));
        reportReserve.setWeedOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getWeedOut(), "weedOut"));
        reportReserve.setToFatten(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToFatten(), "toFatten"));
        reportReserve.setSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "sale"));
        reportReserve.setChgFarmOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getChgFarm(), "chgFarm"));
        reportReserve.setOtherChange(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getOtherChange(), "otherChange"));
        reportReserve.setEnd(groupDaily.getEnd());
        return reportReserve;
    }

    private DoctorReportReserve buildDelayReserve(DoctorGroupDailyExtend groupDaily,
                                                 DoctorReportReserve reportReserve) {
        reportReserve.setDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily, reportReserve.getOrzType()));
        reportReserve.setDailyLivestockOnHand(groupDaily.getDailyLivestockOnHand());
        return reportReserve;
    }

    private void insertOrUpdateReserve(DoctorReportReserve reserve){
        if (isNull(reserve.getId())) {
            doctorReportReserveDao.create(reserve);
            return;
        }
        doctorReportReserveDao.update(reserve);
    }
}
