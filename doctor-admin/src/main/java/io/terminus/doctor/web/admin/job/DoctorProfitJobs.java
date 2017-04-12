package io.terminus.doctor.web.admin.job;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Created by terminus on 2017/4/11.
 */

@RestController
@RequestMapping("/api/profit")
@Slf4j
public class DoctorProfitJobs {

    @Autowired
    private HostLeader hostLeader;

    /**
     * 猪场利润的计算
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
            log.info("daily profit job start, now is:{}", DateUtil.toDateTimeString(new Date()));



            log.info("daily profit job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily profit job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

}
