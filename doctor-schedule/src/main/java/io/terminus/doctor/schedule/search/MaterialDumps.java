package io.terminus.doctor.schedule.search;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.search.material.MaterialDumpService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 物料索引dump
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Slf4j
@RestController
@RequestMapping("/api/search/dump/material")
public class MaterialDumps {

    private final MaterialDumpService materialDumpService;

    private final HostLeader hostLeader;

    @Autowired
    public MaterialDumps(MaterialDumpService materialDumpService, HostLeader hostLeader) {
        this.materialDumpService = materialDumpService;
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
            log.info("material full dump fired");
            materialDumpService.fullDump(null);
            log.info("material full dump end");
        } catch (Exception e) {
            log.error("material full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
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
            log.info("material delta dump fired");
            materialDumpService.deltaDump(15);
            log.info("material delta dump end");
        } catch (Exception e) {
            log.error("material delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }
}
