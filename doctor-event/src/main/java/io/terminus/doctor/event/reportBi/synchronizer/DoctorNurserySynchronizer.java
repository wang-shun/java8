package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.event.dao.DoctorWarehouseReportDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportNurseryDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.reportBi.helper.FieldHelper;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class DoctorNurserySynchronizer {
    private final DoctorReportNurseryDao doctorReportNurseryDao;
    private final FieldHelper fieldHelper;
    private final DoctorWarehouseReportDao doctorWarehouseReportDao;

    @Autowired
    public DoctorNurserySynchronizer(DoctorReportNurseryDao doctorReportNurseryDao, FieldHelper fieldHelper, DoctorWarehouseReportDao doctorWarehouseReportDao) {
        this.doctorReportNurseryDao = doctorReportNurseryDao;
        this.fieldHelper = fieldHelper;
        this.doctorWarehouseReportDao = doctorWarehouseReportDao;
    }

    public void synchronize(DoctorGroupDailyExtend groupDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportNursery reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportNurseryDao.findByDimension(dimensionCriteria))) {
            reportBI= new DoctorReportNursery();
            reportBI.setOrzType(dimensionCriteria.getOrzType());
            reportBI.setDateType(dimensionCriteria.getDateType());
        }
        insertOrUpdate(build(groupDaily, reportBI, dimensionCriteria.getIsRealTime()));
    }

    public DoctorReportNursery build(DoctorGroupDailyExtend groupDaily, DoctorReportNursery reportBi, Integer isRealTime) {
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

    private void buildRealTime(DoctorGroupDailyExtend groupDaily, DoctorReportNursery reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillGroupFiledUrl(filedUrlCriteria, groupDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setStart(groupDaily.getStart());
        reportBi.setTurnInto(fieldHelper.filedUrl(filedUrlCriteria, fieldHelper.groupTurnInto(groupDaily, reportBi.getOrzType()), "turnInto"));
        reportBi.setSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "sale"));
        reportBi.setToHoubei(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToHoubei(), "toHoubei"));
        reportBi.setToFatten(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToFatten(), "toFatten"));
        reportBi.setChgFarmOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getChgFarm(), "chgFarm"));
        reportBi.setDead(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getDead(), "dead"));
        reportBi.setWeedOut(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getWeedOut(), "weedOut"));
        reportBi.setOtherChange(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getOtherChange(), "otherChange"));
        reportBi.setEnd(groupDaily.getEnd());
    }

    private void buildDelay(DoctorGroupDailyExtend groupDaily, DoctorReportNursery reportBi) {
        reportBi.setTurnIntoAge(FieldHelper.getInteger(fieldHelper.turnIntoAge(groupDaily, reportBi.getOrzType()), fieldHelper.groupTurnInto(groupDaily, reportBi.getOrzType())));
        reportBi.setTurnIntoAvgWeight(EventUtil.getAvgWeight(fieldHelper.turnIntoWeight(groupDaily, reportBi.getOrzType()), fieldHelper.groupTurnInto(groupDaily, reportBi.getOrzType())));
        reportBi.setSaleAvgWeight(EventUtil.getAvgWeight(groupDaily.getSaleWeight(), groupDaily.getSale()));
        reportBi.setToHoubeiAvgWeight(EventUtil.getAvgWeight(groupDaily.getToHoubeiWeight(), groupDaily.getToHoubei()));
        reportBi.setToFattenAvgWeight(EventUtil.getAvgWeight(groupDaily.getToFattenWeight(), groupDaily.getToFatten()));
        reportBi.setChgFarmAvgWeight(EventUtil.getAvgWeight(groupDaily.getChgFarmWeight(), groupDaily.getChgFarm()));
        reportBi.setDailyPigCount(groupDaily.getDailyLivestockOnHand());
        reportBi.setOutAvgWeight70(outAvgWeight70(groupDaily));
        if (DateDimension.YEARLY.contains(reportBi.getDateType())) {
            reportBi.setDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily, reportBi.getOrzType()));
            reportBi.setLivingRate(1 - reportBi.getDeadWeedOutRate());
            reportBi.setFeedMeatRate(feedMeatRate(groupDaily,
                    new DoctorDimensionCriteria(reportBi.getOrzId(), reportBi.getOrzType(), reportBi.getSumAt(),
                            reportBi.getDateType(), groupDaily.getPigType())));
        }
    }

    private Double outAvgWeight70(DoctorGroupDailyExtend dailyExtend) {
        Integer STANDARD_AGE = 70;
        Double FACTOR = 1.55;

//        (标准日龄-实际日龄+实际重量*系数)/系数
//        标准日龄=70；系数=1.55；
//        实际重量是指保育出栏头均重；
//        实际日龄是指保育出栏平均日龄
//        实际日龄 = 平均转出日期 - 平均出生日期 - 1
//        实际日龄=结束猪群转出实际总日龄/结束猪群转出实际数量
//        实际重量 = 转出总重量 / 转出总头数
        if(FieldHelper.get(dailyExtend.getTurnActualAge(), dailyExtend.getTurnActualCount()) == 0&&EventUtil.getAvgWeight(dailyExtend.getTurnActualWeight(), dailyExtend.getTurnActualCount())==0){
            return 0.0;
        }else {
            return (STANDARD_AGE - FieldHelper.get(dailyExtend.getTurnActualAge(), dailyExtend.getTurnActualCount())
                    + EventUtil.getAvgWeight(dailyExtend.getTurnActualWeight(), dailyExtend.getTurnActualCount()) * FACTOR) / FACTOR;
        }
    }

    private Double feedMeatRate(DoctorGroupDailyExtend dailyExtend, DoctorDimensionCriteria dimensionCriteria){
        return EventUtil.divide(doctorWarehouseReportDao.materialApply(dimensionCriteria), dailyExtend.getNetWeightGain());
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
