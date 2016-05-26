package io.terminus.doctor.schedule.search;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.search.barn.BarnSearchDumpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 猪舍索引dump
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Slf4j
@Configuration
@EnableScheduling
@RestController
@RequestMapping("/api/doctor/search")
public class BarnDumps {

    @Autowired
    private BarnSearchDumpService barnSearchDumpService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void fullDump() {
        try {
            log.info("full dump fired");
            barnSearchDumpService.fullDump(null);
            log.info("full dump end");
        } catch (Exception e) {
            log.error("barn full dump failed", Throwables.getStackTraceAsString(e));
        }
    }

    @RequestMapping(value = "/barn/full")
    public void fullDump(@RequestParam(required = false) String before) {
        try {
            log.info("full dump fired");
            barnSearchDumpService.fullDump(before);
            log.info("full dump end");
        } catch (Exception e) {
            log.error("barn full dump failed", Throwables.getStackTraceAsString(e));
        }
    }

    @Scheduled(cron = "0 */15 * * * ?")
    @RequestMapping(value = "/barn/delta")
    public void deltaDump() {
        try{
            log.info("delta dump fired");
            barnSearchDumpService.deltaDump(15);
            log.info("delta dump end");
        } catch (Exception e) {
            log.error("barn delta dump failed", Throwables.getStackTraceAsString(e));
        }
    }
}
