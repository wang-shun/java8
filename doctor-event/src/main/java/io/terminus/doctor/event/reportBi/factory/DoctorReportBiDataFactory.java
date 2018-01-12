package io.terminus.doctor.event.reportBi.factory;


import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorReportReserve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.event.reportBi.helper.DateHelper.dateCN;
import static io.terminus.doctor.event.reportBi.helper.DateHelper.withDateStartDay;

/**
 * Created by xjn on 18/1/11.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiDataFactory {

    private final DoctorPigDailyDao doctorPigDailyDao;
    private final DoctorGroupDailyDao doctorGroupDailyDao;

    @Autowired
    public DoctorReportBiDataFactory(DoctorPigDailyDao doctorPigDailyDao, DoctorGroupDailyDao doctorGroupDailyDao) {
        this.doctorPigDailyDao = doctorPigDailyDao;
        this.doctorGroupDailyDao = doctorGroupDailyDao;
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

    public DoctorReportReserve buildReserve(DoctorGroupDailyExtend groupDaily,
                                            DoctorReportReserve reportReserve) {


        if (Objects.equals(reportReserve.getOrzType(), OrzDimension.FARM.getName())) {
            reportReserve.setOrzId(groupDaily.getFarmId());
            reportReserve.setOrzName(groupDaily.getFarmName());
        }
        DateDimension dateDimension = DateDimension.from(reportReserve.getDateType());
        reportReserve.setSumAt(withDateStartDay(groupDaily.getSumAt(), dateDimension));
        reportReserve.setSumAtName(dateCN(groupDaily.getSumAt(), dateDimension));
        buildRealTimeReserve(groupDaily, reportReserve);
        buildDelayReserve(groupDaily, reportReserve);
        return reportReserve;
    }

    public DoctorReportReserve buildRealTimeReserve(DoctorGroupDailyExtend groupDaily,
                                                    DoctorReportReserve reportReserve) {
        reportReserve.setStart(groupDaily.getStart());
        reportReserve.setTurnInto(filedUrl(groupDaily.getTurnInto()));
        reportReserve.setTurnSeed(filedUrl(groupDaily.getTurnSeed()));
        reportReserve.setTurnSeed(filedUrl(groupDaily.getTurnSeed()));
        reportReserve.setDead(filedUrl(groupDaily.getDead()));
        reportReserve.setWeedOut(filedUrl(groupDaily.getWeedOut()));
        reportReserve.setToFatten(filedUrl(groupDaily.getToFatten()));
        reportReserve.setSale(filedUrl(groupDaily.getSale()));
        reportReserve.setChgFarmOut(filedUrl(groupDaily.getChgFarm()));
        reportReserve.setOtherChange(filedUrl(groupDaily.getOtherChange()));
        reportReserve.setEnd(groupDaily.getEnd());
        return reportReserve;
    }

    public DoctorReportReserve buildDelayReserve(DoctorGroupDailyExtend groupDaily,
                                                 DoctorReportReserve reportReserve) {
        reportReserve.setDeadWeedOutRate(deadWeedOutRate(groupDaily, reportReserve));
        reportReserve.setDailyLivestockOnHand(groupDaily.getDailyLivestockOnHand());
        return reportReserve;
    }

    public DoctorReportReserve buildRealTimeSow() {
        return null;
    }

    private DoctorGroupDailyExtend dailyExpand(){
        return null;
    }
    private Double deadWeedOutRate(DoctorGroupDaily groupDaily,
                                   DoctorReportReserve reportReserve){
        return null;
    }
    private String filedUrl(Object obj) {
        return null;
    }
}
