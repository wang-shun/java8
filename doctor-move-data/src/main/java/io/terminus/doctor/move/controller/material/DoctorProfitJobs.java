package io.terminus.doctor.move.controller.material;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by terminus on 2017/4/11.
 */

@RestController
@RequestMapping("/api/profit")
@Slf4j
public class DoctorProfitJobs {


    private final HostLeader hostLeader;

    @Autowired
    private DoctorFarmReadService doctorFarmReadService;
    @Autowired
    private DoctorGroupProfitManage doctorGroupProfitManage;

    @Autowired
    public DoctorProfitJobs(HostLeader hostLeader) {
        this.hostLeader = hostLeader;
    }

    /**
     * 猪场利润的计算
     * 每天凌晨1点统计昨天的数据
     */
    @Scheduled(cron = "0 4 1 * * ?")
    @RequestMapping(value = "/profit", method = RequestMethod.GET)
    public void profitReport() {

        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("daily profit job start, now is:{}", DateUtil.toDateTimeString(new Date()));
            List<Long> farmIds = getAllFarmIds();
            doctorGroupProfitManage.sumDoctorProfitMaterialOrPig(farmIds, new Date());
            log.info("daily profit job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily profit job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/profit/{farmId}")
    public void refreshProfitReport(@PathVariable Long farmId,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }

            Date refreshDate = null == date ? new Date() : date;
            log.info("daily profit job start, now is:{}", DateUtil.toDateTimeString(refreshDate));

            doctorGroupProfitManage.sumDoctorProfitMaterialOrPig(Collections.singletonList(farmId), refreshDate);
            log.info("daily profit job end, now is:{}", DateUtil.toDateTimeString(refreshDate));
        } catch (Exception e) {
            log.error("daily profit job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    private List<Long> getAllFarmIds() {
        return RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public void profitReportAll() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("daily all profit job start, now is:{}", DateUtil.toDateTimeString(new Date()));
            List<Long> farmIds = getAllFarmIds();


            for (int i = 0; i <= 6; i++) {
                int finalI = i;
                new Thread(new Runnable() {
                    Date dateStart = DateUtils.addYears(DateUtil.toDate("2011-01-01"), finalI);
                    Date nowDate = DateUtil.monthStart(DateUtils.addYears(dateStart, 1));

                    @Override
                    public void run() {
                        Date date = dateStart;
                        while (!DateUtil.inSameYearMonth(date, nowDate)) {
                            log.info("profit job time, now is:{},{}", DateUtil.toDateTimeString(date), Thread.currentThread().getName());
                            doctorGroupProfitManage.sumDoctorProfitMaterialOrPig(farmIds, date);
                            date = DateUtils.addMonths(date, 1);
                        }
                    }
                }, "-->>" + i + "<<--").start();
            }

            log.info("daily all profit job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily all profit job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }
}
