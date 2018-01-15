package io.terminus.doctor.event.reportBi.synchronizer;

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
public class DoctorDeliverSynchronizer {
    private final DoctorReportDeliverDao doctorReportDeliverDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorDeliverSynchronizer(DoctorReportDeliverDao doctorReportDeliverDao, FieldHelper fieldHelper) {
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.fieldHelper = fieldHelper;
    }

    public void synchronize(DoctorPigDailyExtend pigDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportDeliver reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportDeliverDao.findByDimension(dimensionCriteria))) {
            OrzDimension orzDimension = expectNotNull(OrzDimension.from(dimensionCriteria.getOrzType()), "orzType.is.illegal");
            DateDimension dateDimension = expectNotNull(DateDimension.from(dimensionCriteria.getDateType()), "dateType.is.illegal");
            reportBI= new DoctorReportDeliver();
            reportBI.setOrzType(orzDimension.getName());
            reportBI.setDateType(dateDimension.getName());
        }
        insertOrUpdate(build(pigDaily, reportBI));
    }

    public void synchronize(DoctorGroupDailyExtend groupDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportDeliver reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportDeliverDao.findByDimension(dimensionCriteria))) {
            OrzDimension orzDimension = expectNotNull(OrzDimension.from(dimensionCriteria.getOrzType()), "orzType.is.illegal");
            DateDimension dateDimension = expectNotNull(DateDimension.from(dimensionCriteria.getDateType()), "dateType.is.illegal");
            reportBI= new DoctorReportDeliver();
            reportBI.setOrzType(orzDimension.getName());
            reportBI.setDateType(dateDimension.getName());
        }
        insertOrUpdate(build(groupDaily, reportBI));
    }

    private void insertOrUpdate(DoctorReportDeliver reportBi){
        if (isNull(reportBi.getId())) {
            doctorReportDeliverDao.create(reportBi);
            return;
        }
        doctorReportDeliverDao.update(reportBi);
    }

    public DoctorReportDeliver build(DoctorPigDailyExtend pigDaily, DoctorReportDeliver reportBi) {
        if (Objects.equals(reportBi.getOrzType(), OrzDimension.FARM.getName())) {
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
        buildDelay(pigDaily, reportBi);
        return reportBi;
    }

    public DoctorReportDeliver build(DoctorGroupDailyExtend dailyExtend, DoctorReportDeliver reportBi) {
        if (Objects.equals(reportBi.getOrzType(), OrzDimension.FARM.getName())) {
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
        buildDelay(dailyExtend, reportBi);
        return reportBi;
    }

    private void buildRealTime(DoctorPigDailyExtend pigDaily, DoctorReportDeliver reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillPigFiledUrl(filedUrlCriteria, pigDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setStart(pigDaily.getSowCfStart());
        reportBi.setEnd(pigDaily.getSowCfEnd());
        reportBi.setSowCfIn(pigDaily.getSowCfIn());
        reportBi.setOtherIn(pigDaily.getSowPhChgFarmIn());
        reportBi.setDead(pigDaily.getSowCfDead());
        reportBi.setWeedOut(pigDaily.getSowCfWeedOut());
        reportBi.setSale(pigDaily.getSowCfSale());
        reportBi.setChgFarmOut(pigDaily.getSowCfChgFarm());
        reportBi.setSowPhWeanOut(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getSowCfIn(), "sowPhWeanOut"));
        reportBi.setOtherChange(pigDaily.getSowCfOtherOut());

        reportBi.setEarlyMating(0);
        reportBi.setEarlyNest(pigDaily.getEarlyFarrowNest());
        reportBi.setEarlyNestRate(fieldHelper.get(pigDaily.getFarrowNest(), reportBi.getEarlyMating()));
        reportBi.setLaterNest(0);
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
        reportBi.setPigletWeakCountPerFarrow(fieldHelper.get(reportBi.getFarrowWeak(), pigDaily.getFarrowNest()));
        reportBi.setAvgWeightPerFarrow(EventUtil.getAvgWeight(pigDaily.getFarrowWeight(), pigDaily.getFarrowNest()));
        reportBi.setFirstBornWeight(EventUtil.getAvgWeight(pigDaily.getFarrowWeight(), pigDaily.getFarrowLive()));
        reportBi.setWeanNest(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getWeanNest(), "weanNest"));
        reportBi.setWeanCount(pigDaily.getWeanCount());
        reportBi.setWeanQualifiedCount(pigDaily.getWeanQualifiedCount());
        reportBi.setWeanCountPerFarrow(fieldHelper.get(pigDaily.getWeanCount(), pigDaily.getWeanNest()));
        reportBi.setWeanDayAge(EventUtil.get(pigDaily.getWeanDayAge(), pigDaily.getWeanCount()));
        reportBi.setWeanWeightPerFarrow(EventUtil.getAvgWeight(pigDaily.getWeanWeight(), pigDaily.getWeanCount()));
    }

    private void buildRealTime(DoctorGroupDailyExtend groupDaily, DoctorReportDeliver reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillGroupFiledUrl(filedUrlCriteria, groupDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setPigletStart(groupDaily.getStart());
        reportBi.setPigletOtherIn(0);
        reportBi.setPigletChgFarmOut(groupDaily.getChgFarm());
        reportBi.setPigletChgFarmOutAvgWeight(EventUtil.getAvgWeight(groupDaily.getChgFarmWeight(), groupDaily.getChgFarm()));
        reportBi.setToNursery(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getToNursery(), "toNursery"));
        reportBi.setToNurseryAvgWeight(0.0);
        reportBi.setPigletDead(groupDaily.getDead());
        reportBi.setPigletWeedOut(groupDaily.getWeedOut());
        reportBi.setPigletOtherChange(groupDaily.getOtherChange());
        reportBi.setPigletDeadWeedOutRate(fieldHelper.deadWeedOutRate(groupDaily,reportBi.getOrzType() ));
        reportBi.setPigletLivingRate(1 - reportBi.getPigletDeadWeedOutRate());
        reportBi.setTurnOutAvgWeight(0.0);
        reportBi.setTurnOutDay(0);
        reportBi.setTurnOutAvgWeight28(0.0);
        reportBi.setPigletSale(fieldHelper.filedUrl(filedUrlCriteria, groupDaily.getSale(), "pigletSale"));
        reportBi.setPigletSaleAveWeight(EventUtil.getAvgWeight(groupDaily.getSaleWeight(), groupDaily.getSale()));
        reportBi.setPigletEnd(groupDaily.getEnd());
    }

    private void buildDelay(DoctorPigDailyExtend pigDaily, DoctorReportDeliver reportBi) {
    }

    private void buildDelay(DoctorGroupDailyExtend groupDaily, DoctorReportDeliver reportBi) {
    }

//    private Integer pigletOtherIn(DoctorGroupDailyExtend groupDaily, String orzType){
//        Integer otherIn =
//    }

    public void deleteAll() {
        doctorReportDeliverDao.deleteAll();
    }
}
