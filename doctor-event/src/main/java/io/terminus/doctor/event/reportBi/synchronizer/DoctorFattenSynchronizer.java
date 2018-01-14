package io.terminus.doctor.event.reportBi.synchronizer;

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

import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/13.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorFattenSynchronizer {
    private final DoctorReportFattenDao doctorReportFattenDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorFattenSynchronizer(DoctorReportFattenDao doctorReportFattenDao, FieldHelper fieldHelper) {
        this.doctorReportFattenDao = doctorReportFattenDao;
        this.fieldHelper = fieldHelper;
    }

    public void synchronize(DoctorGroupDailyExtend groupDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportFatten reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportFattenDao.findByDimension(dimensionCriteria))) {
            OrzDimension orzDimension = expectNotNull(OrzDimension.from(dimensionCriteria.getOrzType()), "orzType.is.illegal");
            DateDimension dateDimension = expectNotNull(DateDimension.from(dimensionCriteria.getDateType()), "dateType.is.illegal");
            reportBI= new DoctorReportFatten();
            reportBI.setOrzType(orzDimension.getName());
            reportBI.setDateType(dateDimension.getName());
        }
        insertOrUpdate(build(groupDaily, reportBI));
    }

    public DoctorReportFatten build(DoctorGroupDailyExtend groupDaily, DoctorReportFatten reportBi) {
        buildRealTime(groupDaily, reportBi);
        buildDelay(groupDaily, reportBi);
        return reportBi;
    }

    private void buildRealTime(DoctorGroupDailyExtend groupDaily, DoctorReportFatten reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillGroupFiledUrl(filedUrlCriteria, groupDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setStart(groupDaily.getStart());
        reportBi.setTurnInto(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getTurnInto(), "turnInto"));
        reportBi.setSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "sale"));
        reportBi.setToHoubei(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToHoubei(), "toHoubei"));
        reportBi.setChgFarmOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getChgFarm(), "chgFarm"));
        reportBi.setDead(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getDead(), "dead"));
        reportBi.setWeedOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getWeedOut(), "weedOut"));
        reportBi.setOtherChange(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getOtherChange(), "otherChange"));
        reportBi.setEnd(groupDaily.getStart());
    }

    private void buildDelay(DoctorGroupDailyExtend groupDaily, DoctorReportFatten reportBi) {
        reportBi.setTurnIntoAge(groupDaily.getTurnIntoAge());
        reportBi.setSaleAvgWeight(EventUtil.getAvgWeight(groupDaily.getSaleWeight(), groupDaily.getSale()));
        reportBi.setToHoubeiAvgWeight(EventUtil.getAvgWeight(groupDaily.getToHoubeiWeight(), groupDaily.getToHoubei()));
        reportBi.setChgFarmAvgWeight(EventUtil.getAvgWeight(groupDaily.getChgFarmWeight(), groupDaily.getChgFarm()));
        reportBi.setDailyPigCount(groupDaily.getDailyLivestockOnHand());
        reportBi.setOutAvgWeight180(0.0);
        reportBi.setDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily, reportBi.getOrzName()));
        reportBi.setLivingRate(1 - reportBi.getDeadWeedOutRate());
        reportBi.setFeedMeatRate(0.0);
    }

    private void insertOrUpdate(DoctorReportFatten reportBi){
        if (isNull(reportBi.getId())) {
            doctorReportFattenDao.create(reportBi);
            return;
        }
        doctorReportFattenDao.update(reportBi);
    }

}
