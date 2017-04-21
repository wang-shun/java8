package io.terminus.doctor.web.admin.job.material;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorGroupMaterialWriteServer;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by terminus on 2017/4/18.
 */
@RestController
@RequestMapping("/api/ware/house")
@Slf4j
public class DoctorWareHouseJobs {

    @RpcConsumer
    private DoctorGroupMaterialWriteServer doctorGroupMaterialWriteServer;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorMaterialManage doctorMaterialManage;

    private final HostLeader hostLeader;
    @Autowired
    public DoctorWareHouseJobs(HostLeader hostLeader) {
        this.hostLeader = hostLeader;
    }
    private final static Integer WHARE = 1;
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0 */1 * * * ?")
    @RequestMapping(value = "/house", method = RequestMethod.GET)
    public void groupMaterialReport() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            doctorGroupMaterialWriteServer.deleteDoctorGroupMaterial(WHARE);
            log.info("daily ware house job start, now is:{}", DateUtil.toDateTimeString(new Date()));
            doctorMaterialManage.runDoctorGroupMaterialWareHouse(getAllFarmIds(), WHARE);
            log.info("daily ware house job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily report job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    private List<Long> getAllFarmIds() {
        return RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }
}
