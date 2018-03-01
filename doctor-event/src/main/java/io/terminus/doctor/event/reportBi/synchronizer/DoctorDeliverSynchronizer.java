package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.reportBi.DoctorReportDeliverDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.dto.reportBi.DoctorPigDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportDeliver;
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
public class DoctorDeliverSynchronizer {
    private final DoctorReportDeliverDao doctorReportDeliverDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorDeliverSynchronizer(DoctorReportDeliverDao doctorReportDeliverDao, FieldHelper fieldHelper) {
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.fieldHelper = fieldHelper;
    }

    public void synchronize(DoctorPigDailyExtend pigDaily,
                            DoctorDimensionCriteria dimensionCriteria) {
        DoctorReportDeliver reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportDeliverDao.findByDimension(dimensionCriteria))) {
            reportBI = new DoctorReportDeliver();
            reportBI.setOrzType(dimensionCriteria.getOrzType());
            reportBI.setDateType(dimensionCriteria.getDateType());
        }
        insertOrUpdate(build(pigDaily, reportBI, dimensionCriteria.getIsRealTime()));
    }

    public void synchronize(DoctorGroupDailyExtend groupDaily,
                            DoctorDimensionCriteria dimensionCriteria) {
        DoctorReportDeliver reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportDeliverDao.findByDimension(dimensionCriteria))) {

            log.info("deliver report not found,create it.sum at[{}],orz id[{}], orz type[{}],date type[{}]",
                    DateUtil.toDateString(dimensionCriteria.getSumAt()),
                    dimensionCriteria.getOrzId(),
                    dimensionCriteria.getOrzType(),
                    dimensionCriteria.getDateType());

            reportBI = new DoctorReportDeliver();
            reportBI.setOrzType(dimensionCriteria.getOrzType());
            reportBI.setDateType(dimensionCriteria.getDateType());
        }
        insertOrUpdate(build(groupDaily, reportBI, dimensionCriteria.getIsRealTime()));
    }

    private void insertOrUpdate(DoctorReportDeliver reportBi) {
        if (isNull(reportBi.getId())) {
            doctorReportDeliverDao.create(reportBi);
            return;
        }
        doctorReportDeliverDao.update(reportBi);
    }

    public DoctorReportDeliver build(DoctorPigDailyExtend pigDaily, DoctorReportDeliver reportBi, Integer isRealTime) {
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

    public DoctorReportDeliver build(DoctorGroupDailyExtend dailyExtend, DoctorReportDeliver reportBi, Integer isRealTime) {
        if (Objects.equals(reportBi.getOrzType(), OrzDimension.FARM.getValue())) {
            reportBi.setOrzId(dailyExtend.getFarmId());
            reportBi.setOrzName(dailyExtend.getFarmName());
        } else {
            reportBi.setOrzId(dailyExtend.getOrgId());
            reportBi.setOrzName(dailyExtend.getOrgName());
        }
        DateDimension dateDimension = DateDimension.from(reportBi.getDateType());
        reportBi.setSumAt(withDateStartDay(dailyExtend.getSumAt(), dateDimension));
        reportBi.setSumAtName(dateCN(dailyExtend.getSumAt(), dateDimension));
        buildRealTime(dailyExtend, reportBi);
        if (!Objects.equals(isRealTime, IsOrNot.YES.getKey())) {
            buildDelay(dailyExtend, reportBi);
        }
        return reportBi;
    }

    private void buildRealTime(DoctorPigDailyExtend pigDaily, DoctorReportDeliver reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillPigFiledUrl(filedUrlCriteria, pigDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setStart(pigDaily.getSowCfStart());
        reportBi.setEnd(pigDaily.getSowCfEnd());
        reportBi.setSowCfIn(pigDaily.getSowCfIn());
        reportBi.setOtherIn(otherIn(pigDaily, reportBi.getOrzType()));
        reportBi.setDead(pigDaily.getSowCfDead());
        reportBi.setWeedOut(pigDaily.getSowCfWeedOut());
        reportBi.setSale(pigDaily.getSowCfSale());
        reportBi.setChgFarmOut(pigDaily.getSowCfChgFarm());
        reportBi.setSowPhWeanOut(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getSowPhWeanIn(), "sowPhWeanOut"));
        reportBi.setOtherChange(pigDaily.getSowCfOtherOut());

        reportBi.setFarrowNest(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getFarrowNest(), "farrowNest"));
        reportBi.setFarrowAll(pigDaily.getFarrowLive() + pigDaily.getFarrowjmh() + pigDaily.getFarrowDead());
        reportBi.setFarrowLiving(pigDaily.getFarrowLive());
        reportBi.setFarrowHealth(pigDaily.getFarrowHealth());
        reportBi.setFarrowWeak(pigDaily.getFarrowWeak());
        reportBi.setFarrowDead(pigDaily.getFarrowDead());
        reportBi.setFarrowJmh(pigDaily.getFarrowjmh());
        reportBi.setPigletCountPerFarrow(fieldHelper.get(reportBi.getFarrowAll(), pigDaily.getFarrowNest()));
        reportBi.setPigletLivingCountPerFarrow(fieldHelper.get(reportBi.getFarrowLiving(), pigDaily.getFarrowNest()));
        reportBi.setPigletHealthCountPerFarrow(fieldHelper.get(reportBi.getFarrowHealth(), pigDaily.getFarrowNest()));
        reportBi.setWeanNest(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getWeanNest(), "weanNest"));
        reportBi.setWeanCount(pigDaily.getWeanCount());
        reportBi.setWeanQualifiedCount(pigDaily.getWeanQualifiedCount());
        reportBi.setWeanCountPerFarrow(fieldHelper.get(pigDaily.getWeanCount(), pigDaily.getWeanNest()));
    }

    private void buildDelay(DoctorPigDailyExtend pigDaily, DoctorReportDeliver reportBi) {
        reportBi.setPigletWeakCountPerFarrow(fieldHelper.get(reportBi.getFarrowWeak(), pigDaily.getFarrowNest()));
        reportBi.setAvgWeightPerFarrow(EventUtil.getAvgWeight(pigDaily.getFarrowWeight(), pigDaily.getFarrowNest()));
        reportBi.setFirstBornWeight(EventUtil.getAvgWeight(pigDaily.getFarrowWeight(), pigDaily.getFarrowLive()));
        reportBi.setWeanDayAge(FieldHelper.getInteger(pigDaily.getWeanDayAge(), pigDaily.getWeanCount()));
        reportBi.setWeanWeightPerFarrow(EventUtil.getAvgWeight(pigDaily.getWeanWeight(), pigDaily.getWeanCount()));
        if (DateDimension.YEARLY.contains(reportBi.getDateType())) {
            reportBi.setEarlyNestRate(fieldHelper.get(pigDaily.getFarrowNest(), pigDaily.getEarlyMating()));
            reportBi.setLaterNestRate(fieldHelper.get(pigDaily.getLaterNest(), pigDaily.getMatingCount()));
            reportBi.setPigletLivingRate(1 - FieldHelper.get(EventUtil.plusInt(reportBi.getPigletDead(), reportBi.getPigletWeedOut()), reportBi.getFarrowHealth()));
        }
        reportBi.setEarlyMating(pigDaily.getEarlyMating());
        reportBi.setEarlyNest(pigDaily.getEarlyFarrowNest());
        reportBi.setLaterNest(pigDaily.getLaterNest());
        reportBi.setTurnOutAvgWeight28(outAvgWeight28(reportBi));
    }

    private void buildRealTime(DoctorGroupDailyExtend groupDaily, DoctorReportDeliver reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillGroupFiledUrl(filedUrlCriteria, groupDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setPigletStart(groupDaily.getStart());
        reportBi.setPigletOtherIn(otherIn(groupDaily, reportBi.getOrzType()));
        reportBi.setPigletChgFarmOut(groupDaily.getChgFarm());
        reportBi.setToNursery(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToNursery(), "toNursery"));
        reportBi.setPigletDead(groupDaily.getDead());
        reportBi.setPigletWeedOut(groupDaily.getWeedOut());
        reportBi.setPigletOtherChange(groupDaily.getOtherChange());
        reportBi.setPigletSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "pigletSale"));
        reportBi.setPigletEnd(groupDaily.getEnd());
    }

    private void buildDelay(DoctorGroupDailyExtend groupDaily, DoctorReportDeliver reportBi) {
        reportBi.setPigletChgFarmOutAvgWeight(EventUtil.getAvgWeight(groupDaily.getChgFarmWeight(), groupDaily.getChgFarm()));
        reportBi.setToNurseryAvgWeight(EventUtil.getAvgWeight(groupDaily.getToNurseryWeight(), groupDaily.getToNursery()));
        if (DateDimension.YEARLY.contains(reportBi.getDateType())) {
            reportBi.setPigletDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily, reportBi.getOrzType()));
            reportBi.setPigletLivingRate(1 - FieldHelper.get(EventUtil.plusInt(reportBi.getPigletDead(), reportBi.getPigletWeedOut()), reportBi.getFarrowHealth()));
        }
        reportBi.setTurnOutAvgWeight(EventUtil.getAvgWeight(EventUtil.plusDouble(groupDaily.getToNurseryWeight(), groupDaily.getSaleWeight()),
                EventUtil.plusInt(groupDaily.getToNursery(), groupDaily.getSale())));
        reportBi.setTurnOutDay(FieldHelper.getInteger(groupDaily.getDeliverTurnOutAge(), EventUtil.plusInt(groupDaily.getToNursery(), groupDaily.getSale())));
        reportBi.setPigletSaleAveWeight(EventUtil.getAvgWeight(groupDaily.getSaleWeight(), groupDaily.getSale()));
    }

    public Integer otherIn(DoctorPigDailyExtend dailyExtend, Integer orzType) {
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            return dailyExtend.getSowCfInFarmIn();
        }
        return 0;
    }

    public Integer otherIn(DoctorGroupDailyExtend dailyExtend, Integer orzType) {
        int otherIn = dailyExtend.getDeliverHandTurnInto();
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            return otherIn + dailyExtend.getChgFarmIn();
        }
        return otherIn;
    }

    private Double outAvgWeight28(DoctorReportDeliver deliver) {
        Integer STANDARD_AGE = 28;
        return (EventUtil.getAvgWeight(deliver.getWeanWeightPerFarrow(), deliver.getWeanDayAge()))
                * (STANDARD_AGE - deliver.getWeanDayAge())
                + deliver.getWeanWeightPerFarrow();
    }

    public void deleteAll() {
        doctorReportDeliverDao.deleteAll();
    }
}
