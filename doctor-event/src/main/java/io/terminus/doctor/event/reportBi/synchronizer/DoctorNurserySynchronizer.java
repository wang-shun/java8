package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.event.dao.reportBi.DoctorReportNurseryDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.reportBi.helper.FieldHelper;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.event.reportBi.helper.DateHelper.dateCN;
import static io.terminus.doctor.event.reportBi.helper.DateHelper.withDateStartDay;
import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/13.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorNurserySynchronizer {
    private final DoctorReportNurseryDao doctorReportNurseryDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorNurserySynchronizer(DoctorReportNurseryDao doctorReportNurseryDao, FieldHelper fieldHelper) {
        this.doctorReportNurseryDao = doctorReportNurseryDao;
        this.fieldHelper = fieldHelper;
    }

    public void synchronize(DoctorGroupDailyExtend groupDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportNursery reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportNurseryDao.findByDimension(dimensionCriteria))) {
            OrzDimension orzDimension = expectNotNull(OrzDimension.from(dimensionCriteria.getOrzType()), "orzType.is.illegal");
            DateDimension dateDimension = expectNotNull(DateDimension.from(dimensionCriteria.getDateType()), "dateType.is.illegal");
            reportBI= new DoctorReportNursery();
            reportBI.setOrzType(orzDimension.getName());
            reportBI.setDateType(dateDimension.getName());
        }
        insertOrUpdate(build(groupDaily, reportBI));
    }

    public DoctorReportNursery build(DoctorGroupDailyExtend groupDaily, DoctorReportNursery reportBi) {
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

    private void buildRealTime(DoctorGroupDailyExtend groupDaily, DoctorReportNursery reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillGroupFiledUrl(filedUrlCriteria, groupDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setStart(groupDaily.getStart());
        reportBi.setTurnInto(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getTurnInto(), "turnInto"));
        reportBi.setSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "sale"));
        reportBi.setToHoubei(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToHoubei(), "toHoubei"));
        reportBi.setToFatten(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToFatten(), "toFatten"));
        reportBi.setChgFarmOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getChgFarm(), "chgFarm"));
        reportBi.setDead(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getDead(), "dead"));
        reportBi.setWeedOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getWeedOut(), "weedOut"));
        reportBi.setOtherChange(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getOtherChange(), "otherChange"));
        reportBi.setEnd(groupDaily.getStart());
    }

    private void buildDelay(DoctorGroupDailyExtend groupDaily, DoctorReportNursery reportBi) {
        reportBi.setTurnIntoAge(groupDaily.getTurnIntoAge());
        reportBi.setTurnIntoAvgWeight(EventUtil.getAvgWeight(groupDaily.getTurnIntoWeight(), groupDaily.getTurnInto()));
        reportBi.setSaleAvgWeight(EventUtil.getAvgWeight(groupDaily.getSaleWeight(), groupDaily.getSale()));
        reportBi.setToHoubeiAvgWeight(EventUtil.getAvgWeight(groupDaily.getToHoubeiWeight(), groupDaily.getToHoubei()));
        reportBi.setToFattenAvgWeight(EventUtil.getAvgWeight(groupDaily.getToFattenWeight(), groupDaily.getToFatten()));
        reportBi.setChgFarmAvgWeight(EventUtil.getAvgWeight(groupDaily.getChgFarmWeight(), groupDaily.getChgFarm()));
        reportBi.setDailyPigCount(groupDaily.getDailyLivestockOnHand());
        reportBi.setOutAvgWeight70(0.0);
        reportBi.setDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily, reportBi.getOrzName()));
        reportBi.setLivingRate(1 - reportBi.getDeadWeedOutRate());
        reportBi.setFeedMeatRate(0.0);
    }

    private void insertOrUpdate(DoctorReportNursery reportBi){
        if (isNull(reportBi.getId())) {
            doctorReportNurseryDao.create(reportBi);
            return;
        }
        doctorReportNurseryDao.update(reportBi);
    }

    public void deleteAll() {
        doctorReportNurseryDao.deleteAll();
    }
}
