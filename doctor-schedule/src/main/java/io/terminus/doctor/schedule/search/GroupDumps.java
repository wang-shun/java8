package io.terminus.doctor.schedule.search;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.search.group.GroupDumpService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 猪群索引dump
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Slf4j
@RestController
@RequestMapping("/api/search/dump/group")
public class GroupDumps {

    private final GroupDumpService groupDumpService;

    private final HostLeader hostLeader;

    @Autowired
    public GroupDumps(GroupDumpService groupDumpService, HostLeader hostLeader) {
        this.groupDumpService = groupDumpService;
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
            log.info("group full dump fired");
            groupDumpService.fullDump(null);
            log.info("group full dump end");
        } catch (Exception e) {
            log.error("group full dump failed", Throwables.getStackTraceAsString(e));
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
            log.info("group delta dump fired");
            groupDumpService.deltaDump(15);
            log.info("group delta dump end");
        } catch (Exception e) {
            log.error("group delta dump failed", Throwables.getStackTraceAsString(e));
        }
    }
}
