package io.terminus.doctor.web.admin.job;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.service.*;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

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

    @RpcConsumer
    private DoctorDailyReportReadService doctorDailyReportReadService;
    @RpcConsumer(timeout = "60000")
    private DoctorDailyReportWriteService doctorDailyReportWriteService;
    @RpcConsumer(timeout = "60000")
    private DoctorRangeReportWriteService doctorRangeReportWriteService;
    @RpcConsumer(timeout = "60000")
    private DoctorCommonReportWriteService doctorCommonReportWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService;

    @RpcConsumer
    private DoctorBoarMonthlyReportWriteService doctorBoarMonthlyReportWriteService;

    @RpcConsumer(timeout = "60000")
    private DoctorDailyGroupWriteService doctorDailyGroupWriteService;

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
    @RequestMapping(value = "/daily", method = RequestMethod.GET)
    public void dailyReport() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("daily report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            doctorDailyReportWriteService.generateYesterdayAndTodayReports(getAllFarmIds());

            log.info("daily report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @RequestMapping(value = "/group/daily", method = RequestMethod.GET)
    public void groupDaily() {
        try {
            if(!hostLeader.isLeader()){
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("statistics daily group job start, now is:{}", DateUtil.toDateTimeString(new Date()));
            Date yesterday = new DateTime(Dates.startOfDay(new Date())).plusDays(-1).toDate();
            RespHelper.or500(doctorDailyGroupWriteService.generateYesterdayAndToday(getAllFarmIds(),yesterday));
            log.info("statistics daily group job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        }catch (Exception e){
            log.error("statistics daily group job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 猪场月报计算job
     * 每两点执行一发
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @RequestMapping(value = "/range", method = RequestMethod.GET)
    public void monthlyReport() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("range report job start, now is:{}", DateUtil.toDateTimeString(new Date()));
            List<Long> farmIds = getAllFarmIds();
            Date today = Dates.startOfDay(new Date());
            doctorRangeReportWriteService.generateDoctorRangeReports(farmIds, today);
            log.info("range report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("range report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 公猪生产成绩月报计算job
     * 每天凌晨3点统计昨天的数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
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
    @Scheduled(cron = "0 0 3 * * ?")
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

    private List<Long> getAllFarmIds() {
        return RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }
}