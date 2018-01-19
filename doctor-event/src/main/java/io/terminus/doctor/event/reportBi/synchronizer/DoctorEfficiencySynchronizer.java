package io.terminus.doctor.event.reportBi.synchronizer;

import com.sun.org.apache.regexp.internal.RE;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorReportNpdDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportEfficiencyDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import io.terminus.doctor.event.model.DoctorReportNpd;
import io.terminus.doctor.event.reportBi.helper.DateHelper;
import io.terminus.doctor.event.service.DoctorPigReportReadService;
import io.terminus.doctor.event.service.DoctorReportReadService;
import io.terminus.doctor.event.service.DoctorReportWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/1/15.
 */
@Slf4j
@Component
public class DoctorEfficiencySynchronizer {

    @Autowired
    private DoctorReportNpdDao doctorReportNpdDao;
    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;
    @Autowired
    private DoctorReportEfficiencyDao doctorReportEfficiencyDao;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;
    @RpcConsumer
    private DoctorPigReportReadService doctorPigReportReadService;


    public void deleteAll() {
        doctorReportNpdDao.delete();
    }


    /**
     * 从doctor_report_npd表同步到doctor_report_efficiency
     * doctor_report_npd，以猪场和月为单位，一个猪场一个月一条记录
     */
    public void sync(DoctorDimensionCriteria dimensionCriteria) {

        if (dimensionCriteria.getDateType().equals(DateDimension.DAY.getValue())
                || dimensionCriteria.getDateType().equals(DateDimension.WEEK.getValue()))
            return;

        List<DoctorReportNpd> npds = doctorReportNpdDao.count(dimensionCriteria, null, null);
        for (DoctorReportNpd npd : npds) {
            DoctorReportEfficiency efficiency = new DoctorReportEfficiency();
            efficiency.setOrzId(dimensionCriteria.getOrzId());
            efficiency.setOrzType(dimensionCriteria.getOrzType());
            efficiency.setDateType(dimensionCriteria.getDateType());
            efficiency.setSumAtName(DateHelper.dateCN(npd.getSumAt(), DateDimension.from(dimensionCriteria.getDateType())));

            if (dimensionCriteria.getOrzType().equals(OrzDimension.FARM.getValue())) {
                DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(dimensionCriteria.getOrzId()));
                efficiency.setOrzName(farm == null ? "" : farm.getName());
            } else if (dimensionCriteria.getOrzType().equals(OrzDimension.ORG.getValue())) {
                DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(dimensionCriteria.getOrzId()));
                efficiency.setOrzName(org == null ? "" : org.getName());
            }

            Date end;
            if (dimensionCriteria.getDateType().equals(DateDimension.MONTH.getValue()))
                end = DateUtil.monthEnd(npd.getSumAt());
            else if (dimensionCriteria.getDateType().equals(DateDimension.QUARTER.getValue())) {
                end = DateHelper.withDateEndDay(npd.getSumAt(), DateDimension.QUARTER);
            } else {
                end = DateHelper.withDateEndDay(npd.getSumAt(), DateDimension.YEAR);
            }
            DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(dimensionCriteria.getOrzId(), npd.getSumAt(), end);

            efficiency.setSumAt(npd.getSumAt());
            //非生产天数=非生产天数/母猪存栏/天数
            if (npd.getSowCount() != 0) {

                int dayCount = DateUtil.getDeltaDays(npd.getSumAt(), end) + 1;
                efficiency.setNpd(new BigDecimal(npd.getNpd()).divide(new BigDecimal(npd.getSowCount()).divide(new BigDecimal(dayCount), 2, BigDecimal.ROUND_HALF_UP), 2, BigDecimal.ROUND_HALF_UP).intValue());
            }

            //年产胎次（月）=365-非生产天数*12/生产天数/总窝数
            if (null != pigDaily && pigDaily.getFarrowNest() != null && pigDaily.getFarrowNest() != 0 && efficiency.getNpd() != null
                    && (npd.getPregnancy() + npd.getLactation() != 0)) {
                BigDecimal mi = new BigDecimal(npd.getPregnancy()).add(new BigDecimal(npd.getLactation())).divide(new BigDecimal(pigDaily.getFarrowNest()), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal re = new BigDecimal(365).subtract(new BigDecimal(efficiency.getNpd())).multiply(new BigDecimal(12)).divide(mi, 2, BigDecimal.ROUND_HALF_UP);
                efficiency.setBirthPerYear(re.intValue());
            }

            //psy=年产胎次*断奶仔猪数/断奶窝数
            if (null != pigDaily && pigDaily.getWeanNest() != null && pigDaily.getWeanNest() != 0 && efficiency.getBirthPerYear() != null)
                efficiency.setPsy(efficiency.getBirthPerYear() * (pigDaily.getWeanCount() / pigDaily.getWeanNest()));
            efficiency.setPregnancy(npd.getPregnancy());
            efficiency.setLactation(npd.getLactation());

            doctorReportEfficiencyDao.create(efficiency);
        }
    }

    /**
     * 刷新该猪场的指定日期所在的月、季、年
     * 和所在的公司的月、季、年
     *
     * @param farmId
     * @param date
     */
    public void syncFarm(Long farmId, Date date) {

    }

    /**
     * 刷新该公司指定日期所在的月、季、年
     *
     * @param orgId
     * @param date
     */
    public void syncOrg(Long orgId, Date date) {

    }

    /**
     * 刷新所有猪场和公司指定日期所在的月、季、年
     *
     * @param date
     */
    public void sync(Date date) {
        //
        Date start = DateHelper.withDateStartDay(date, DateDimension.MONTH);
        Date end = DateHelper.withDateEndDay(date, DateDimension.MONTH);
        List<DoctorReportNpd> npds = doctorReportNpdDao.findBySumAt(start);
        create(npds, DateDimension.MONTH, start, end);

        DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
        criteria.setOrzType(OrzDimension.FARM.getValue());
        criteria.setDateType(DateDimension.QUARTER.getValue());

        DoctorPigReportReadService.DateDuration dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.SEASON);
        start = dateDuration.getStart();
        end = dateDuration.getEnd();

        npds = doctorReportNpdDao.count(criteria, start, end);
        create(npds, DateDimension.QUARTER, start, end);

        criteria = new DoctorDimensionCriteria();
        criteria.setOrzType(OrzDimension.FARM.getValue());
        criteria.setDateType(DateDimension.YEAR.getValue());

        dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.YEAR);
        start = dateDuration.getStart();
        end = dateDuration.getEnd();

        npds = doctorReportNpdDao.count(criteria, start, end);
        create(npds, DateDimension.YEAR, start, end);
    }

    private void create(List<DoctorReportNpd> npds, DateDimension dateDimension, Date start, Date end) {

        Set<Long> orgIds = new HashSet<>();

        Map<Long, List<DoctorReportNpd>> npdMap = npds.stream().collect(Collectors.groupingBy(DoctorReportNpd::getFarmId));
        RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().forEach(f -> {

            DoctorReportEfficiency efficiency;
            DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
            criteria.setOrzId(f.getId());
            criteria.setDateType(dateDimension.getValue());
            criteria.setOrzType(OrzDimension.FARM.getValue());
            criteria.setSumAt(start);
            efficiency = doctorReportEfficiencyDao.findByDimension(criteria);
            if (null == efficiency) efficiency = new DoctorReportEfficiency();

            if (!npdMap.containsKey(f.getId())) {
                //补
                efficiency.setOrzId(f.getId());
                efficiency.setOrzType(OrzDimension.FARM.getValue());
                efficiency.setDateType(dateDimension.getValue());
                efficiency.setOrzName(f.getName());
                efficiency.setSumAt(start);
                efficiency.setSumAtName(DateHelper.dateCN(start, dateDimension));

                efficiency.setPregnancy(0);
                efficiency.setLactation(0);
                if (null == efficiency.getId())
                    doctorReportEfficiencyDao.create(efficiency);
                else
                    doctorReportEfficiencyDao.update(efficiency);
            } else {
                DoctorReportNpd npd = npdMap.get(f.getId()).get(0);//每个猪场只会有一条，无论是月、季、年
                DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(npd.getFarmId(), start, end);

                efficiency.setOrzId(npd.getFarmId());
                efficiency.setOrzType(OrzDimension.FARM.getValue());
                efficiency.setDateType(dateDimension.getValue());
                efficiency.setOrzName(f.getName());
                efficiency.setSumAtName(DateHelper.dateCN(npd.getSumAt(), dateDimension));

                efficiency.setSumAt(npd.getSumAt());
                //非生产天数=非生产天数/母猪存栏/天数
                if (npd.getSowCount() != 0) {

                    int dayCount = DateUtil.getDeltaDays(start, end) + 1;
                    efficiency.setNpd(new BigDecimal(npd.getNpd()).divide(new BigDecimal(npd.getSowCount()).divide(new BigDecimal(dayCount), 2, BigDecimal.ROUND_HALF_UP), 2, BigDecimal.ROUND_HALF_UP).intValue());
                }

                //年产胎次（月）=365-非生产天数*12/生产天数/总窝数
                if (null != pigDaily && pigDaily.getFarrowNest() != null && pigDaily.getFarrowNest() != 0 && efficiency.getNpd() != null
                        && (npd.getPregnancy() + npd.getLactation() != 0)) {
                    BigDecimal mi = new BigDecimal(npd.getPregnancy()).add(new BigDecimal(npd.getLactation())).divide(new BigDecimal(pigDaily.getFarrowNest()), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal re = new BigDecimal(365).subtract(new BigDecimal(efficiency.getNpd())).multiply(new BigDecimal(12)).divide(mi, 2, BigDecimal.ROUND_HALF_UP);
                    efficiency.setBirthPerYear(re.intValue());
                }

                //psy=年产胎次*断奶仔猪数/断奶窝数
                if (null != pigDaily && pigDaily.getWeanNest() != null && pigDaily.getWeanNest() != 0 && efficiency.getBirthPerYear() != null)
                    efficiency.setPsy(efficiency.getBirthPerYear() * (pigDaily.getWeanCount() / pigDaily.getWeanNest()));
                efficiency.setPregnancy(npd.getPregnancy());
                efficiency.setLactation(npd.getLactation());

                if (efficiency.getId() == null)
                    doctorReportEfficiencyDao.create(efficiency);
                else
                    doctorReportEfficiencyDao.update(efficiency);
                orgIds.add(npd.getOrgId());
            }
        });

        RespHelper.orServEx(doctorOrgReadService.findAllOrgs()).stream().forEach(o -> {

            DoctorReportEfficiency efficiency;
            DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
            criteria.setOrzId(o.getId());
            criteria.setDateType(dateDimension.getValue());
            criteria.setOrzType(OrzDimension.ORG.getValue());
            criteria.setSumAt(start);
            efficiency = doctorReportEfficiencyDao.findByDimension(criteria);
            if (null == efficiency) efficiency = new DoctorReportEfficiency();

            if (!orgIds.contains(o.getId())) {
                //补
                efficiency.setOrzId(o.getId());
                efficiency.setOrzType(OrzDimension.ORG.getValue());
                efficiency.setDateType(dateDimension.getValue());
                efficiency.setOrzName(o.getName());
                efficiency.setSumAt(start);
                efficiency.setSumAtName(DateHelper.dateCN(start, dateDimension));

                efficiency.setPregnancy(0);
                efficiency.setLactation(0);
                if (null == efficiency.getId())
                    doctorReportEfficiencyDao.create(efficiency);
                else
                    doctorReportEfficiencyDao.update(efficiency);
            } else {
                DoctorReportNpd npd = doctorReportNpdDao.findByOrgAndSumAt(o.getId(), start);

                DoctorPigDaily pigDaily = doctorPigDailyDao.countByOrg(o.getId(), start, end);

                efficiency.setOrzId(o.getId());
                efficiency.setOrzType(OrzDimension.ORG.getValue());
                efficiency.setDateType(dateDimension.getValue());
                efficiency.setSumAtName(DateHelper.dateCN(start, dateDimension));
//                DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(orgId));
                efficiency.setOrzName(o.getName());

                efficiency.setSumAt(start);
                //非生产天数=非生产天数/母猪存栏/天数
                if (npd.getSowCount() != 0)
                    efficiency.setNpd(npd.getNpd() / (npd.getSowCount() / npd.getDays()));

                //年产胎次（月）=365-非生产天数*12/生产天数/总窝数
                if (null != pigDaily && pigDaily.getFarrowNest() != null && pigDaily.getFarrowNest() != 0 && efficiency.getNpd() != null)
                    efficiency.setBirthPerYear((365 - efficiency.getNpd()) * 12 / ((npd.getPregnancy() + npd.getLactation()) / pigDaily.getFarrowNest()));

                //psy=年产胎次*断奶仔猪数/断奶窝数
                if (null != pigDaily && pigDaily.getWeanNest() != null && pigDaily.getWeanNest() != 0 && efficiency.getBirthPerYear() != null)
                    efficiency.setPsy(efficiency.getBirthPerYear() * (pigDaily.getWeanCount() / pigDaily.getWeanNest()));
                efficiency.setPregnancy(npd.getPregnancy());
                efficiency.setLactation(npd.getLactation());

                if (efficiency.getId() == null)
                    doctorReportEfficiencyDao.create(efficiency);
                else
                    doctorReportEfficiencyDao.update(efficiency);
            }
        });
    }
}
