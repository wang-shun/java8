package io.terminus.doctor.schedule.event;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorMonthlyReportWriteService;
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
    @RpcConsumer
    private DoctorMonthlyReportWriteService doctorMonthlyReportWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

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
            RespHelper.or500(doctorDailyReportWriteService.createDailyReports(getAllFarmIds(), yesterday));

            log.info("daily report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 更新历史日报
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @RequestMapping(value = "/updateHistoryDailyReport", method = RequestMethod.GET)
    public void updateHistoryDailyReport(){
        Date endDate = new DateTime(Dates.startOfDay(new Date())).plusDays(-2).toDate();
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("update history daily report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            for(Map.Entry<Long, String> entry : RespHelper.or500(doctorDailyReportReadService.getDailyReport2Update()).entrySet()){
                Long farmId = entry.getKey();
                Date beginDate = DateUtil.toDate(entry.getValue());
                RespHelper.or500(doctorDailyReportWriteService.updateDailyReport(beginDate, endDate, farmId));
                RespHelper.or500(doctorDailyReportWriteService.deleteDailyReport2Update(farmId));
                while(!beginDate.after(endDate)){
                    RespHelper.or500(doctorDailyReportWriteService.deleteDailyReportFromRedis(farmId, beginDate));
                    beginDate = new DateTime(beginDate).plusDays(1).toDate();
                }
            }

            log.info("update history daily report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        }catch(Exception e) {
            log.error("update history daily report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }


    /**
     * 猪场月报计算job
     * 每天凌晨3点统计昨天的数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public void monthlyReport() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("monthly report job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            //获取昨天的天初
            Date yesterday = new DateTime(Dates.startOfDay(new Date())).plusDays(-1).toDate();
            List<DoctorDailyReport> dailyReports = RespHelper.orServEx(doctorDailyReportReadService.findDailyReportBySumAt(yesterday));
            if (!notEmpty(dailyReports)) {
                log.error("daily report not found, so can not monthly report!");
                throw new ServiceException("daily.report.find.fail");
            }

            RespHelper.or500(doctorMonthlyReportWriteService.createMonthlyReports(getAllFarmIds(), yesterday));

            log.info("monthly report job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("monthly report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    private List<Long> getAllFarmIds() {
        return RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }
}
