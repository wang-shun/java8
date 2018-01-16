package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorReportNpdDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportEfficiencyDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
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
     *
     * @param npds
     */
    public void sync(List<DoctorReportNpd> npds, DoctorDimensionCriteria dimensionCriteria) {

        if (dimensionCriteria.getDateType().equals(DateDimension.DAY.getValue())
                || dimensionCriteria.getDateType().equals(DateDimension.WEEK.getValue()))
            return;

        //月
        if (dimensionCriteria.getDateType().equals(DateDimension.MONTH.getValue())) {
            DoctorReportNpd npd = npds.get(0);
            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(npd.getFarmId()));

            Date end = DateUtil.monthEnd(npd.getSumAt());

            DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(npd.getFarmId(), npd.getSumAt(), end);

            DoctorReportEfficiency efficiency = new DoctorReportEfficiency();
            efficiency.setSumAt(npd.getSumAt());
            efficiency.setSumAtName(DateHelper.dateCN(npd.getSumAt(), DateDimension.MONTH));
            efficiency.setDateType(DateDimension.MONTH.getValue());
            efficiency.setOrzId(npd.getFarmId());
            efficiency.setOrzName(farm == null ? "" : farm.getName());
            efficiency.setOrzType(OrzDimension.FARM.getValue());
            //非生产天数=非生产天数/母猪存栏/天数
            efficiency.setNpd(npd.getNpd() / (npd.getSowCount() / npd.getDays()));
            //年产胎次（月）=365-非生产天数*12/生产天数/总窝数
            efficiency.setBirthPerYear((365 - efficiency.getNpd()) * 12 / ((npd.getPregnancy() + npd.getLactation()) / pigDaily.getFarrowNest()));
            //psy=年产胎次*断奶仔猪数/断奶窝数
            efficiency.setPsy(efficiency.getBirthPerYear() * (pigDaily.getWeanCount() / pigDaily.getWeanNest()));


            doctorReportEfficiencyDao.create(efficiency);

            if (null != farm) {
                DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(farm.getOrgId()));
                if (null != org) {
                    DoctorReportNpd orgNpd = doctorReportNpdDao.findByOrgAndSumAt(RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(org.getId()))
                            .stream()
                            .map(DoctorFarm::getId)
                            .collect(Collectors.toList()), npd.getSumAt());

                    DoctorReportEfficiency orgEfficiency = new DoctorReportEfficiency();
                    orgEfficiency.setSumAt(orgNpd.getSumAt());
                    orgEfficiency.setSumAtName(DateHelper.dateCN(npd.getSumAt(), DateDimension.MONTH));
                    orgEfficiency.setDateType(DateDimension.MONTH.getValue());
                    orgEfficiency.setOrzId(org.getId());
                    orgEfficiency.setOrzName(org == null ? "" : org.getName());
                    orgEfficiency.setOrzType(OrzDimension.ORG.getValue());
                    //非生产天数=非生产天数/母猪存栏/天数
                    orgEfficiency.setNpd(npd.getNpd() / (npd.getSowCount() / npd.getDays()));
                    //年产胎次（月）=365-非生产天数*12/生产天数/总窝数
                    orgEfficiency.setBirthPerYear((365 - efficiency.getNpd()) * 12 / ((npd.getPregnancy() + npd.getLactation()) / pigDaily.getFarrowNest()));

                    doctorReportEfficiencyDao.create(orgEfficiency);
                }
            }
        }

        //季

        //年

    }
}
