package io.terminus.doctor.schedule.search;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.search.barn.BarnSearchDumpService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 猪舍索引dump
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Slf4j
@RestController
@RequestMapping("/api/search/dump/barn")
public class BarnDumps {

    private final BarnSearchDumpService barnSearchDumpService;

    private final HostLeader hostLeader;

    @Autowired
    public BarnDumps(BarnSearchDumpService barnSearchDumpService, HostLeader hostLeader) {
        this.barnSearchDumpService = barnSearchDumpService;
        this.hostLeader = hostLeader;
    }

    //@Scheduled(cron = "0 0 1 * * ?")
    @RequestMapping(value = "/full", method = RequestMethod.GET)
    public void fullDump() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("barn full dump fired");
            barnSearchDumpService.fullDump(null);
            log.info("barn full dump end");
        } catch (Exception e) {
            log.error("barn full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    //@Scheduled(cron = "0 */15 * * * ?")
    @RequestMapping(value = "/delta", method = RequestMethod.GET)
    public void deltaDump() {
        try{
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("barn delta dump fired");
            barnSearchDumpService.deltaDump(15);
            log.info("barn delta dump end");
        } catch (Exception e) {
            log.error("barn delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * dump 指定时间到现在数据
     * @param before 指定时间
     */
    @RequestMapping(value = "/fullDump", method = RequestMethod.GET)
    public void fullDump(String before) {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("before {}", before);
            log.info("barn full dump fired");
            barnSearchDumpService.fullDump(before);
            log.info("barn full dump end");
        } catch (Exception e) {
            log.error("barn full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

}
