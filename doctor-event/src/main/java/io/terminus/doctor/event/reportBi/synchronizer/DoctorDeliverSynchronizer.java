package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigStatisticDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportDeliverDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMatingDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.dto.reportBi.DoctorPigDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.reportBi.helper.DateHelper;
import io.terminus.doctor.event.reportBi.helper.FieldHelper;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
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
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    private final DoctorPigStatisticDao doctorPigStatisticDao;

    private final DoctorReportMatingDao doctorReportMatingDao;

    @Autowired
    public DoctorDeliverSynchronizer(DoctorReportDeliverDao doctorReportDeliverDao, FieldHelper fieldHelper, DoctorPigStatisticDao doctorPigStatisticDao, DoctorReportMatingDao doctorReportMatingDao) {
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.fieldHelper = fieldHelper;
        this.doctorPigStatisticDao = doctorPigStatisticDao;
        this.doctorReportMatingDao = doctorReportMatingDao;
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
        reportBi.setPigletCountPerFarrow(FieldHelper.get(reportBi.getFarrowAll(), pigDaily.getFarrowNest()));
        reportBi.setPigletLivingCountPerFarrow(FieldHelper.get(reportBi.getFarrowLiving(), pigDaily.getFarrowNest()));
        reportBi.setPigletHealthCountPerFarrow(FieldHelper.get(reportBi.getFarrowHealth(), pigDaily.getFarrowNest()));
        reportBi.setWeanNest(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getWeanNest(), "weanNest"));
        reportBi.setWeanCount(pigDaily.getWeanCount());
        reportBi.setWeanQualifiedCount(pigDaily.getWeanQualifiedCount());
        reportBi.setWeanCountPerFarrow(FieldHelper.get(pigDaily.getWeanCount(), pigDaily.getWeanNest()));
    }

    private void buildDelay(DoctorPigDailyExtend pigDaily, DoctorReportDeliver reportBi) {
        reportBi.setPigletWeakCountPerFarrow(FieldHelper.get(reportBi.getFarrowWeak(), pigDaily.getFarrowNest()));
        reportBi.setAvgWeightPerFarrow(EventUtil.getAvgWeight(pigDaily.getFarrowWeight(), pigDaily.getFarrowNest()));
        reportBi.setFirstBornWeight(EventUtil.getAvgWeight(pigDaily.getFarrowWeight(), pigDaily.getFarrowLive()));
        reportBi.setWeanDayAge(FieldHelper.getInteger(pigDaily.getWeanDayAge(), pigDaily.getWeanCount()));
        reportBi.setWeanWeightPerFarrow(EventUtil.getAvgWeight(pigDaily.getWeanWeight(), pigDaily.getWeanCount()));
        reportBi.setEarlyMating(pigDaily.getEarlyMating());
        // TODO: 18/4/24 单独同步数据
//        reportBi.setEarlyNest(earlyFarrowNest(pigDaily, reportBi.getOrzType(), reportBi.getSumAt(), reportBi.getDateType()));
//        reportBi.setLaterNest(laterFarrowNest(pigDaily, reportBi.getOrzType(), reportBi.getSumAt(), reportBi.getDateType()));
//        if (DateDimension.YEARLY.contains(reportBi.getDateType())) {
//            reportBi.setEarlyNestRate(FieldHelper.get(reportBi.getEarlyNest(), reportBi.getEarlyMating()));
//            reportBi.setLaterNestRate(FieldHelper.get(reportBi.getLaterNest(), pigDaily.getMatingCount()));
//        }
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
            reportBi.setPigletLivingRate(1 - reportBi.getPigletDeadWeedOutRate());
        }
        reportBi.setTurnOutAvgWeight(EventUtil.getAvgWeight(EventUtil.plusDouble(EventUtil.plusDouble(groupDaily.getToNurseryWeight(), groupDaily.getSaleWeight()),groupDaily.getChgFarmWeight()),
                EventUtil.plusInt(EventUtil.plusInt(groupDaily.getToNursery(), groupDaily.getSale()),groupDaily.getChgFarm())));
        reportBi.setTurnOutDay(FieldHelper.getInteger(groupDaily.getDeliverTurnOutAge(), EventUtil.plusInt(EventUtil.plusInt(groupDaily.getToNursery(), groupDaily.getSale()),groupDaily.getChgFarm())));
        reportBi.setPigletSaleAveWeight(EventUtil.getAvgWeight(groupDaily.getSaleWeight(), groupDaily.getSale()));
    }

    private Integer otherIn(DoctorPigDailyExtend dailyExtend, Integer orzType) {
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            return dailyExtend.getSowCfInFarmIn();
        }
        return 0;
    }

    private Integer otherIn(DoctorGroupDailyExtend dailyExtend, Integer orzType) {
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

    private Integer earlyFarrowNest(Long orzId, Integer orzType, Date sumAt, Integer dateType) {
        DateDimension dateDimension = DateDimension.from(dateType);
        String startAt = DateUtil.toDateString(new DateTime(DateHelper.withDateStartDay(sumAt, dateDimension)).minusDays(114).toDate());
        String endAt = DateUtil.toDateString(new DateTime(DateHelper.withDateEndDay(sumAt, dateDimension)).minusDays(114).toDate());
        return farrowNest(orzId, orzType, startAt, endAt);
    }

    private Integer laterFarrowNest(Long orzId, Integer orzType, Date sumAt, Integer dateType) {
        DateDimension dateDimension = DateDimension.from(dateType);
        String startAt = DateUtil.toDateString(DateHelper.withDateStartDay(sumAt, dateDimension));
        String endAt = DateUtil.toDateString(DateHelper.withDateEndDay(sumAt, dateDimension));
        return farrowNest(orzId, orzType, startAt, endAt);
    }

    private Integer farrowNest(Long orzId, Integer orzType, String startAt, String endAt){
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            return doctorPigStatisticDao.mateLeadToFarrow(orzId, startAt, endAt);
        } else if (Objects.equals(orzType, OrzDimension.ORG.getValue())) {
            int earlyFarrowNest = 0;
            List<DoctorFarm> farmList = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(orzId));
            for (DoctorFarm farm: farmList) {
                earlyFarrowNest += doctorPigStatisticDao.mateLeadToFarrow(farm.getId(), startAt, endAt);
            }
            return earlyFarrowNest;
        }else{
            int earlyFarrowNest = 0;
            List<DoctorFarm> farmList = RespHelper.orServEx(doctorFarmReadService.findFarmsByGroupId(orzId));
            for (DoctorFarm farm: farmList) {
                earlyFarrowNest += doctorPigStatisticDao.mateLeadToFarrow(farm.getId(), startAt, endAt);
            }
            return earlyFarrowNest;
        }
    }
    public void deleteAll() {
        doctorReportDeliverDao.deleteAll();
    }

    /**
     * 计算前推分娩数，后推分娩数，前推分娩率，后推分娩率
     */
    public void synchronizeDeliverRate(DoctorDimensionCriteria dimensionCriteria) {
        DoctorReportDeliver reportBi = doctorReportDeliverDao.findByDimension(dimensionCriteria);
        DoctorReportMating doctorReportMating = doctorReportMatingDao.findByDimension(dimensionCriteria);
        Integer matingCount = doctorReportMating.getMatingCount();
        reportBi.setEarlyNest(earlyFarrowNest(reportBi.getOrzId(), reportBi.getOrzType(), reportBi.getSumAt(), reportBi.getDateType()));
        reportBi.setLaterNest(laterFarrowNest(reportBi.getOrzId(), reportBi.getOrzType(), reportBi.getSumAt(), reportBi.getDateType()));
        if (DateDimension.YEARLY.contains(reportBi.getDateType())) {
            reportBi.setEarlyNestRate(FieldHelper.get(reportBi.getEarlyNest(), reportBi.getEarlyMating()));
            reportBi.setLaterNestRate(FieldHelper.get(reportBi.getLaterNest(), matingCount));
        }
        doctorReportDeliverDao.update(reportBi);
    }
}
