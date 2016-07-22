package io.terminus.doctor.schedule.event;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Desc: 猪场统计job controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/13
 */
@Slf4j
@RestController
@RequestMapping("/api/job/statistic")
public class DoctorStatisticJobs {

    private final HostLeader hostLeader;
    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;
    private final DoctorFarmReadService doctorFarmReadService;

    @Autowired
    public DoctorStatisticJobs(HostLeader hostLeader,
                               DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService,
                               DoctorFarmReadService doctorFarmReadService) {
        this.hostLeader = hostLeader;
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     * 猪场猪类存栏统计job full
     * 每天凌晨2点统计一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @RequestMapping(value = "/pigType/full", method = RequestMethod.GET)
    public void fullStatisticPigType() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("fullStatisticPigType job start");
            statisticPigType();
            log.info("fullStatisticPigType job end");
        } catch (Exception e) {
            log.error("fullStatisticPigType job failed", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 猪场猪类存栏统计job delta
     * 每6个小时统计一次
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void deltaStatisticPigType() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("deltaStatisticPigType job start");
            statisticPigType();
            log.info("deltaStatisticPigType job end");
        } catch (Exception e) {
            log.error("deltaStatisticPigType job failed", Throwables.getStackTraceAsString(e));
        }
    }

    //遍历每个猪场进行统计
    private void statisticPigType() {
        List<DoctorFarm> farms = RespHelper.or500(doctorFarmReadService.findAllFarms());
        farms.forEach(farm -> {
            doctorPigTypeStatisticWriteService.statisticGroup(farm.getOrgId(), farm.getId());
            doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PIG_TYPE.BOAR.getKey());
            doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PIG_TYPE.SOW.getKey());
        });
    }
}
