package io.terminus.doctor.web.admin.job;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.service.DoctorBoarMonthlyReportWriteService;
import io.terminus.doctor.event.service.DoctorCommonReportWriteService;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorParityMonthlyReportWriteService;
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
    @RpcConsumer
    private DoctorDailyReportWriteService doctorDailyReportWriteService;
    @RpcConsumer(timeout = "60000")
    private DoctorCommonReportWriteService doctorCommonReportWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService;

    @RpcConsumer
    private DoctorBoarMonthlyReportWriteService doctorBoarMonthlyReportWriteService;

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

            //获取昨天的最后一秒(必须是昨天, 因为统计日期设置的是此字段)
            Date yesterday = new DateTime(Dates.startOfDay(new Date())).plusSeconds(-1).toDate();
            doctorDailyReportWriteService.createDailyReports(getAllFarmIds(), yesterday);
            log.info("yesterday daily report job end, now is:{}", DateUtil.toDateTimeString(new Date()));

            //生成一下当天的日报，为月报做准备
            doctorDailyReportWriteService.createDailyReports(getAllFarmIds(), new Date());
            log.info("today daily report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 更新历史日报和月报
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @RequestMapping(value = "/updateHistoryReport", method = RequestMethod.GET)
    public void updateHistoryReport(){
        Date endDate = new DateTime(Dates.startOfDay(new Date())).plusDays(-1).toDate(); // 昨天的开始时间
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("update history report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            for(Map.Entry<Long, String> entry : RespHelper.or500(doctorDailyReportReadService.getDailyReport2Update()).entrySet()){
                Long farmId = entry.getKey();
                Date beginDate = DateUtil.toDate(entry.getValue()); // 自此日期之后(包括此日期)的日报和月报应当被更新
                if(beginDate == null) {
                    continue;
                }

                //日报更新
                Date daily = new Date(beginDate.getTime());
                while(!daily.after(endDate)){
                    RespHelper.or500(doctorDailyReportWriteService.createDailyReports(Lists.newArrayList(farmId), daily));
                    daily = new DateTime(daily).plusDays(1).toDate();
                }
                RespHelper.or500(doctorDailyReportWriteService.deleteDailyReport2Update(farmId));

                // 周报更新
                Date weekly = new DateTime(beginDate).withDayOfWeek(7).toDate();
                while (!weekly.after(endDate)) {
                    RespHelper.or500(doctorCommonReportWriteService.createWeeklyReport(farmId, weekly));
                    weekly = new DateTime(weekly).plusWeeks(1).toDate();
                }

                // 月报更新
                Date monthly = new Date(beginDate.getTime());
                while(!DateUtil.inSameYearMonth(monthly, endDate)){
                    RespHelper.or500(doctorCommonReportWriteService.createMonthlyReport(farmId, DateUtil.getMonthEnd(new DateTime(monthly)).toDate()));
                    monthly = new DateTime(monthly).plusMonths(1).toDate();
                }
            }

            log.info("update history report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        }catch(Exception e) {
            log.error("update history report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 猪场月报计算job
     * 每两点执行一发
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public void monthlyReport() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("monthly report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            //获取今天的天初
            Date today = Dates.startOfDay(new Date());
            List<DoctorDailyReport> dailyReports = RespHelper.orServEx(doctorDailyReportReadService.findDailyReportBySumAt(today));
            if (!notEmpty(dailyReports)) {
                log.error("daily report not found, so can not monthly report!");
                throw new ServiceException("daily.report.find.fail");
            }

            List<Long> farmIds = getAllFarmIds();
            farmIds.forEach(farmId -> doctorCommonReportWriteService.createMonthlyReport(farmId, today));
            farmIds.forEach(farmId -> doctorCommonReportWriteService.update4MonthReports(farmId, today));
            log.info("monthly report job end, now is:{}", DateUtil.toDateTimeString(new Date()));

            farmIds.forEach(farmId -> doctorCommonReportWriteService.createWeeklyReport(farmId, today));
            log.info("weekly report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("monthly and weekly report job failed, cause:{}", Throwables.getStackTraceAsString(e));
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