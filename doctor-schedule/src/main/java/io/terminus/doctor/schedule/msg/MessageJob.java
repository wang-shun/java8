package io.terminus.doctor.schedule.msg;

import com.google.common.base.Throwables;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 消息 job
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@RestController
@RequestMapping("/api/msg")
@Slf4j
public class MessageJob {

    @Autowired
    private HostLeader hostLeader;

    @Autowired
    private MsgManager msgManager;

    /**
     * 产生消息
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @RequestMapping(value = "/produce", method = RequestMethod.GET)
    public void messageProduce() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("message produce fired");
            msgManager.produce();
            log.info("message produce end");
        } catch (Exception e) {
            log.error("message produce failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 消费短信消息
     */
    //@Scheduled(cron = "0 */15 * * * ?")
    @RequestMapping(value = "/consume/sms", method = RequestMethod.GET)
    public void messageConsume() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("msg message consume fired");
            //msgManager.consumeMsg();
            log.info("msg message consume end");
        } catch (Exception e) {
            log.error("msg message consume failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 消费邮件消息
     */
    @Scheduled(cron = "0 */15 * * * ?")
    @RequestMapping(value = "/consume/email", method = RequestMethod.GET)
    public void emailConsume() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("msg message consume fired");
           // msgManager.consumeEmail();
            log.info("msg message consume end");
        } catch (Exception e) {
            log.error("msg message consume failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 消费 app push 消息
     */
    @Scheduled(cron = "0 */15 * * * ?")
    @RequestMapping(value = "/consume/app", method = RequestMethod.GET)
    public void appPushConsume() {
        try {
            if (!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("app push message consume fired");
          //  msgManager.consumeAppPush();
            log.info("app push message consume end");
        } catch (Exception e) {
            log.error("app push message consume failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }
}
