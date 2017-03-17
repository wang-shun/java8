package io.terminus.doctor.web.admin.job;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.EventRequestStatus;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.service.DoctorEventModifyRequestReadService;
import io.terminus.doctor.event.service.DoctorEventModifyRequestWriteService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by xjn on 17/3/16.
 * 事件编辑请求job
 */
@Slf4j
@RestController
@RequestMapping("/admin/modifyRequest")
public class DoctorEventModifyRequestJobs {
    @RpcConsumer
    private DoctorEventModifyRequestWriteService doctorEventModifyRequestWriteService;
    @RpcConsumer
    private DoctorEventModifyRequestReadService doctorEventModifyRequestReadService;
    @Autowired
    HostLeader hostLeader;
    /**
     * 处理事件编辑前请求job 、
     * 每五分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @RequestMapping(value = "/handle", method = RequestMethod.GET)
    public void modifyRequestHandle() {
        log.info("modifyRequestHandle, starting");
        if(!hostLeader.isLeader()) {
            log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
            return;
        }
        List<DoctorEventModifyRequest> handlingList = RespHelper.or500(doctorEventModifyRequestReadService.listByStatus(EventRequestStatus.HANDLING.getValue()));
        List<DoctorEventModifyRequest> waitingList = RespHelper.or500(doctorEventModifyRequestReadService.listByStatus(EventRequestStatus.WAITING.getValue()));

        if (!waitingList.isEmpty() && handlingList.isEmpty()) {
            doctorEventModifyRequestWriteService.modifyRequestHandleJob(waitingList);
        }
        log.info("modifyRequestHandle, ending");
    }
}
