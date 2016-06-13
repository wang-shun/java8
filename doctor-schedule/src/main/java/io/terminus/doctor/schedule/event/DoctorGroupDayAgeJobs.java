package io.terminus.doctor.schedule.event;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/13
 */
@Slf4j
@RestController
@RequestMapping("/api/job/group")
public class DoctorGroupDayAgeJobs {

    private final HostLeader hostLeader;
    private final DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    public DoctorGroupDayAgeJobs(HostLeader hostLeader,
                                 DoctorGroupWriteService doctorGroupWriteService) {
        this.hostLeader = hostLeader;
        this.doctorGroupWriteService = doctorGroupWriteService;
    }

    /**
     * 猪群日龄计算job
     * 每天凌晨3点统计一次
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @RequestMapping(value = "/dayAge", method = RequestMethod.GET)
    public void incrDayAge() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("group incrDayAge job start");
            doctorGroupWriteService.incrDayAge();
            log.info("group incrDayAge job end");
        } catch (Exception e) {
            log.error("group incrDayAge job failed", Throwables.getStackTraceAsString(e));
        }
    }
}
