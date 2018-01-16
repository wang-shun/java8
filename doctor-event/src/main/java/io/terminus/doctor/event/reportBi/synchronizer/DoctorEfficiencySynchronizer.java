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
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/1/15.
 */
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

        List<DoctorReportNpd> npds = doctorReportNpdDao.count(dimensionCriteria);
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
}
