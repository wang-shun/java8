package io.terminus.doctor.web.admin.job;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.service.*;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorDepartmentReadService;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Desc: 猪场日报job
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */
@Slf4j
@RestController
@RequestMapping("/api/job/report")
public class DoctorReportJobs {


    @RpcConsumer(timeout = "60000")
    private DoctorCommonReportWriteService doctorCommonReportWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService;

    @RpcConsumer
    private DoctorBoarMonthlyReportWriteService doctorBoarMonthlyReportWriteService;

    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;
    @RpcConsumer
    private DoctorDepartmentReadService doctorDepartmentReadService;
    @RpcConsumer
    private DoctorDailyReportV2Service doctorDailyReportV2Service;
    @RpcConsumer
    private DoctorReportWriteService doctorReportWriteService;

    private final HostLeader hostLeader;

    @Autowired
    public DoctorReportJobs(HostLeader hostLeader) {
        this.hostLeader = hostLeader;
    }

    /**
     * 猪场日报计算job
     * 每天凌晨1点统计昨天的数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @RequestMapping(value = "/dailyV2", method = RequestMethod.GET)
    public void dailyReportV2() {
        try {
            if(!hostLeader.isLeader())
            {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("daily report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            Date yestoday = DateTime.now().minusDays(1).toDate();
            doctorDailyReportV2Service.generateYesterdayAndToday(getAllFarmIds(), yestoday);

            log.info("daily report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 公猪生产成绩月报计算job
     * 每天凌晨3点统计昨天的数据
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @RequestMapping(value = "/boarMonthly", method = RequestMethod.GET)
    public void boarMonthlyReport() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("boar monthly report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            DateUtil.getBeforeMonthEnds(DateTime.now().plusDays(-1).toDate(), 4)
                    .forEach(date -> doctorBoarMonthlyReportWriteService.createMonthlyReports(getAllFarmIds(), date));

            log.info("boar monthly report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("boar monthly report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 猪场胎次产仔月报计算job
     * 每天凌晨3点统计昨天的数据
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @RequestMapping(value = "/parityMonthly", method = RequestMethod.GET)
    public void parityMonthlyReport() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("parity monthly report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            //获取昨天的天初
            Date yesterday = new DateTime(Dates.startOfDay(new Date())).plusDays(-1).toDate();
            RespHelper.or500(doctorParityMonthlyReportWriteService.createMonthlyReports(getAllFarmIds(), yesterday));

            log.info("parity monthly report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("parity monthly report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    @Scheduled(cron = "0 0 6 * * ?")
    @RequestMapping(value = "/deliver/rate", method = RequestMethod.GET)
    public void deliverRate() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
           /* log.info("deliver rate report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            //获取昨天的天初
            Date yesterday = DateTime.now().minusDays(1).toDate();
            RespHelper.or500(doctorDailyReportV2Service.generateDeliverRate(getAllFarmIds(), yesterday));

            log.info("deliver rate report job end, now is:{}", DateUtil.toDateTimeString(new Date()));*/
            log.info("synchronize all deliver rate starting start:{}");
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.DAY_OF_MONTH,cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            Date start = cal.getTime();
            start.setHours(0);
            start.setMinutes(0);
            start.setSeconds(0);
            List<DoctorFarm> doctorFarms = RespHelper.orServEx(doctorFarmReadService.findAllFarms());
            Set<Long> orzList = doctorFarms.stream().map(DoctorFarm::getId).collect(Collectors.toSet());
            orzList.parallelStream().forEach(orzId ->
                    doctorDailyReportV2Service.flushDeliverRate(orzId, OrzDimension.FARM.getValue(), start));

            orzList = doctorFarms.stream().map(DoctorFarm::getOrgId).collect(Collectors.toSet());
            orzList.parallelStream().forEach(orzId ->
                    doctorDailyReportV2Service.flushDeliverRate(orzId, OrzDimension.ORG.getValue(), start));
            log.info("synchronize all deliver rate end");
        } catch (Exception e) {
            log.error("deliver rate  report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }
    @Scheduled(cron = "0 0 4 18 * ?")
    @RequestMapping(value = "/month/npd", method = RequestMethod.GET)
    public void monthNpd(){
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("npd monthly job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            //获取上一个月
            Date month = new DateTime(Dates.startOfDay(new Date())).plusMonths(-1).toDate();
            log.info("month ===>",  month);
            doctorReportWriteService.flushNPD(getAllFarmIds(), month, ReportTime.MONTH);

            log.info("npd monthly job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("parity monthly report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }
    private List<Long> getAllFarmIds() {
        return RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }

    private Map<Long, List<Long>> getOrgToFarm() {
        Map<Long, List<Long>> orgMapToFarm = Maps.newHashMap();
        List<DoctorOrg> orgList = RespHelper.orServEx(doctorOrgReadService.findAllOrgs());
        orgList.forEach(doctorOrg -> {
            Response<List<DoctorFarm>> farmResponse = doctorDepartmentReadService.findAllFarmsByOrgId(doctorOrg.getId());
            if (farmResponse.isSuccess()) {
                orgMapToFarm.put(doctorOrg.getId(), farmResponse.getResult().stream().map(DoctorFarm::getId).collect(Collectors.toList()));
            }
        });
        return orgMapToFarm;
    }
}