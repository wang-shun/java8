package io.terminus.doctor.schedule.search;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.search.pig.PigDumpService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 猪索引dump
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Slf4j
@RestController
@RequestMapping("/api/search/dump/pig")
public class PigDumps {

    private final PigDumpService pigDumpService;

    private final HostLeader hostLeader;

    @Autowired
    public PigDumps(PigDumpService pigDumpService, HostLeader hostLeader) {
        this.pigDumpService = pigDumpService;
        this.hostLeader = hostLeader;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @RequestMapping(value = "/full", method = RequestMethod.GET)
    public void fullDump() {
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("pig full dump fired");
            pigDumpService.fullDump(null);
            log.info("pig full dump end");
        } catch (Exception e) {
            log.error("pig full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    @Scheduled(cron = "0 */15 * * * ?")
    @RequestMapping(value = "/delta", method = RequestMethod.GET)
    public void deltaDump() {
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("pig delta dump fired");
            pigDumpService.deltaDump(15);
            log.info("pig delta dump end");
        } catch (Exception e) {
            log.error("pig delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * dump 指定时间到现在数据
     * @param before 指定时间
     */
    @RequestMapping(value = "/fullDump", method = RequestMethod.GET)
    public void fullDump(String before) {
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("before {}", before);
            log.info("pig full dump fired");
            pigDumpService.fullDump(before);
            log.info("pig full dump end");
        } catch (Exception e) {
            log.error("pig full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }
}
