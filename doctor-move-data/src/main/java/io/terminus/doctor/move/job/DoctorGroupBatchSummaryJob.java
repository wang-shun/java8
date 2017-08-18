package io.terminus.doctor.move.job;

import com.google.common.base.Throwables;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by xjn on 17/6/5.
 * 猪群批次总结
 */
@Slf4j
@EnableScheduling
@RestController
@RequestMapping("/api/doctor/batchSummary")
public class DoctorGroupBatchSummaryJob {

    @Autowired
    private HostLeader hostLeader;
    @Autowired
    private DoctorGroupBatchSummaryManager manager;

    @Scheduled(cron = "0 0 3 * * ?")
    @RequestMapping("/generate")
    public void generateGroupBatchSummary() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("group batch summary job starting");
            manager.createAllGroupSummary();
            log.info("group batch summary job ending");
        } catch (Exception e) {
            log.error("group batch summary job failed, cause:{}"
                    , Throwables.getStackTraceAsString(e));
        }
    }
}
