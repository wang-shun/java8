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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            efficiency.setNpd(npd.getNpd() / (npd.getSowCount() / npd.getDays()));
            //年产胎次（月）=365-非生产天数*12/生产天数/总窝数
            efficiency.setBirthPerYear((365 - efficiency.getNpd()) * 12 / ((npd.getPregnancy() + npd.getLactation()) / pigDaily.getFarrowNest()));
            //psy=年产胎次*断奶仔猪数/断奶窝数
            efficiency.setPsy(efficiency.getBirthPerYear() * (pigDaily.getWeanCount() / pigDaily.getWeanNest()));
            efficiency.setPregnancy(npd.getPregnancy());
            efficiency.setLactation(npd.getLactation());

            doctorReportEfficiencyDao.create(efficiency);
        }
    }

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

        for (DoctorReportNpd npd : npds) {

            DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(npd.getFarmId(), start, end);

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(npd.getFarmId()));

            DoctorReportEfficiency efficiency = new DoctorReportEfficiency();
            efficiency.setOrzId(npd.getFarmId());
            efficiency.setOrzType(OrzDimension.FARM.getValue());
            efficiency.setDateType(dateDimension.getValue());
            efficiency.setOrzName(null == farm ? "" : farm.getName());
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

//            doctorReportEfficiencyDao.create(efficiency);
            orgIds.add(npd.getOrgId());
        }


        for (Long orgId : orgIds) {

            DoctorReportNpd npd = doctorReportNpdDao.findByOrgAndSumAt(orgId, start);

            DoctorPigDaily pigDaily = doctorPigDailyDao.countByOrg(orgId, start, end);

            log.info("准备同步公司的效率指标【{}】,{}", npd.getOrgId(), pigDaily.getFarrowNest(), pigDaily.getWeanNest());

            DoctorReportEfficiency efficiency = new DoctorReportEfficiency();
            efficiency.setOrzId(orgId);
            efficiency.setOrzType(OrzDimension.ORG.getValue());
            efficiency.setDateType(dateDimension.getValue());
            efficiency.setSumAtName(DateHelper.dateCN(start, dateDimension));
            DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(orgId));
            efficiency.setOrzName(org == null ? "" : org.getName());

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

//            doctorReportEfficiencyDao.create(efficiency);
        }
    }
}
