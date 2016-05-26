package io.terminus.doctor.schedule.search;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.search.group.GroupDumpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Slf4j
@Configuration
@EnableScheduling
@RestController
@RequestMapping("/api/doctor/search")
public class GroupDumps {

    @Autowired
    private GroupDumpService groupDumpService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void fullDump() {
        try{
            log.info("full dump fired");
            groupDumpService.fullDump(null);
            log.info("full dump end");
        } catch (Exception e) {
            log.error("group full dump failed", Throwables.getStackTraceAsString(e));
        }
    }

    @RequestMapping(value = "/group/full")
    public void fullDump(@RequestParam(required = false) String before) {
        try{
            log.info("full dump fired");
            groupDumpService.fullDump(before);
            log.info("full dump end");
        } catch (Exception e) {
            log.error("group full dump failed", Throwables.getStackTraceAsString(e));
        }
    }

    @Scheduled(cron = "0 */15 * * * ?")
    @RequestMapping(value = "/group/delta")
    public void deltaDump() {
        try{
            log.info("delta dump fired");
            groupDumpService.deltaDump(15);
            log.info("delta dump end");
        } catch (Exception e) {
            log.error("delta dump failed", Throwables.getStackTraceAsString(e));
        }
    }
}
