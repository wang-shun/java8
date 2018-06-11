package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.event.dao.DoctorWarehouseReportDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportFattenDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportFatten;
import io.terminus.doctor.event.reportBi.helper.FieldHelper;
import io.terminus.doctor.event.util.EventUtil;
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
public class DoctorFattenSynchronizer {
    private final DoctorReportFattenDao doctorReportFattenDao;
    private final DoctorWarehouseReportDao doctorWarehouseReportDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorFattenSynchronizer(DoctorReportFattenDao doctorReportFattenDao,
                                    DoctorWarehouseReportDao doctorWarehouseReportDao,
                                    FieldHelper fieldHelper) {
        this.doctorReportFattenDao = doctorReportFattenDao;
        this.doctorWarehouseReportDao = doctorWarehouseReportDao;
        this.fieldHelper = fieldHelper;
    }

    public void synchronize(DoctorGroupDailyExtend groupDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportFatten reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportFattenDao.findByDimension(dimensionCriteria))) {
            reportBI= new DoctorReportFatten();
            reportBI.setOrzType(dimensionCriteria.getOrzType());
            reportBI.setDateType(dimensionCriteria.getDateType());
        }
        insertOrUpdate(build(groupDaily, reportBI, dimensionCriteria.getIsRealTime()));
    }

    public DoctorReportFatten build(DoctorGroupDailyExtend groupDaily, DoctorReportFatten reportBi, Integer isRealTime) {
        if (Objects.equals(reportBi.getOrzType(), OrzDimension.FARM.getValue())) {
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
        if (!Objects.equals(isRealTime, IsOrNot.YES.getKey())) {
            buildDelay(groupDaily, reportBi);
        }
        return reportBi;
    }

    private void buildRealTime(DoctorGroupDailyExtend groupDaily, DoctorReportFatten reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillGroupFiledUrl(filedUrlCriteria, groupDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setStart(groupDaily.getStart());
        reportBi.setTurnInto(fieldHelper.filedUrl(filedUrlCriteria, fieldHelper.groupTurnInto(groupDaily, reportBi.getOrzType()), "turnInto"));
        reportBi.setSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "sale"));
        reportBi.setToHoubei(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToHoubei(), "toHoubei"));
        reportBi.setChgFarmOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getChgFarm(), "chgFarm"));
        reportBi.setDead(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getDead(), "dead"));
        reportBi.setWeedOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getWeedOut(), "weedOut"));
        reportBi.setOtherChange(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getOtherChange(), "otherChange"));
        reportBi.setEnd(groupDaily.getEnd());
    }

    private void buildDelay(DoctorGroupDailyExtend groupDaily, DoctorReportFatten reportBi) {
        reportBi.setTurnIntoAge(FieldHelper.getInteger(fieldHelper.turnIntoAge(groupDaily, reportBi.getOrzType()), fieldHelper.groupTurnInto(groupDaily, reportBi.getOrzType())));
        reportBi.setTurnIntoAvgWeight(EventUtil.getAvgWeight(fieldHelper.turnIntoWeight(groupDaily, reportBi.getOrzType()), fieldHelper.groupTurnInto(groupDaily, reportBi.getOrzType())));
        reportBi.setSaleAvgWeight(EventUtil.getAvgWeight(groupDaily.getSaleWeight(), groupDaily.getSale()));
        reportBi.setToHoubeiAvgWeight(EventUtil.getAvgWeight(groupDaily.getToHoubeiWeight(), groupDaily.getToHoubei()));
        reportBi.setChgFarmAvgWeight(EventUtil.getAvgWeight(groupDaily.getChgFarmWeight(), groupDaily.getChgFarm()));
        reportBi.setDailyPigCount(groupDaily.getDailyLivestockOnHand());
        reportBi.setOutAvgWeight180(outAvgWeight180(groupDaily));
        if (DateDimension.YEARLY.contains(reportBi.getDateType())) {
            reportBi.setDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily, reportBi.getOrzType()));
            reportBi.setLivingRate(1 - reportBi.getDeadWeedOutRate());
            reportBi.setFeedMeatRate(feedMeatRate(groupDaily,
                    new DoctorDimensionCriteria(reportBi.getOrzId(), reportBi.getOrzType(), reportBi.getSumAt(),
                            reportBi.getDateType(), groupDaily.getPigType())));
        }
    }

    private Double outAvgWeight180(DoctorGroupDailyExtend dailyExtend) {
        Integer STANDARD_AGE = 180;
        Double FACTOR = 1.77;
        if(FieldHelper.get(dailyExtend.getTurnActualAge(), dailyExtend.getTurnActualCount()) == 0){
            return 0.0;
        } else {
            return (STANDARD_AGE - FieldHelper.get(dailyExtend.getTurnActualAge(), dailyExtend.getTurnActualCount())
                    + EventUtil.getAvgWeight(dailyExtend.getTurnActualWeight(), dailyExtend.getTurnActualCount()) * FACTOR) / FACTOR;
        }
    }

    private Double feedMeatRate(DoctorGroupDailyExtend dailyExtend,DoctorDimensionCriteria dimensionCriteria){
        return EventUtil.divide(doctorWarehouseReportDao.materialApply(dimensionCriteria), dailyExtend.getNetWeightGain());
    }

    private void insertOrUpdate(DoctorReportFatten reportBi){
        if (isNull(reportBi.getId())) {
            doctorReportFattenDao.create(reportBi);
            return;
        }
        doctorReportFattenDao.update(reportBi);
    }

    public void deleteAll() {
        doctorReportFattenDao.deleteAll();
    }
}
