package io.terminus.doctor.web.admin.job;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorGroupInfoCheckWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
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
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 20:24 2017/3/25
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/data/check")
public class DoctorDataCheckJobs {
    @Autowired
    HostLeader hostLeader;
    @RpcConsumer
    private DoctorGroupInfoCheckWriteService doctorGroupInfoCheckWriteService;

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    /**
     * 检查group的数据是否正确
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @RequestMapping( value = "/daily", method = RequestMethod.GET)
    public void generateGroupCheckDatas(){
        if(!hostLeader.isLeader()) {
            log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
            return;
        }
        try{
            log.info("generateGroupCheckDatas starting, now is: {}", DateUtil.toDateTimeString(new Date()));
            List<DoctorFarm> farmLists = RespHelper.or500(doctorFarmReadService.findAllFarms());
            List<Long> farmIds = farmLists.stream().map(DoctorFarm::getId).collect(Collectors.toList());
            doctorGroupInfoCheckWriteService.generateGroupCheckDatas(farmIds);
            log.info("generateGroupCheckDatas end, now is: {}", DateUtil.toDateTimeString(new Date()));
        }catch(Exception e){
            log.error("generateGroupCheckDatas failed, cause: {}", Throwables.getStackTraceAsString(e));
        }

    }
}
