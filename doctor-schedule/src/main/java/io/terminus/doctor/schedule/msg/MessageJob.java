package io.terminus.doctor.schedule.msg;

import com.google.common.base.Throwables;
import io.terminus.doctor.msg.service.DoctorMessageJob;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@Component
@Configurable
@EnableScheduling
@Slf4j
public class MessageJob {

    @Autowired
    private HostLeader hostLeader;

    @Autowired
    private DoctorMessageJob doctorMessageJob;

    /**
     * 产生消息
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void messageProduce() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("message produce fired");
            doctorMessageJob.produce();
            log.info("message produce end");
        } catch (Exception e) {
            log.error("message produce failed", Throwables.getStackTraceAsString(e));
        }
    }

}
