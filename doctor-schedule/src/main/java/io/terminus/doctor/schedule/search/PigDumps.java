package io.terminus.doctor.schedule.search;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.search.pig.PigDumpService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 猪索引dump
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Slf4j
@Configuration
@EnableScheduling
@Component
public class PigDumps {

    @Autowired
    private PigDumpService pigDumpService;

    @Autowired
    private HostLeader hostLeader;

    @Scheduled(cron = "0 0 1 * * ?")
    public void fullDump() {
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("full dump fired");
            pigDumpService.fullDump(null);
            log.info("full dump end");
        } catch (Exception e) {
            log.error("pig full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    @Scheduled(cron = "0 */15 * * * ?")
    public void deltaDump() {
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("delta dump fired");
            pigDumpService.deltaDump(15);
            log.info("delta dump end");
        } catch (Exception e) {
            log.error("pig delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }
}
