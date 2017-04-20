package io.terminus.doctor.web.admin.job;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorProfitExportDto;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorProfitMaterOrPigWriteServer;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by terminus on 2017/4/11.
 */

@RestController
@RequestMapping("/api/profit")
@Slf4j
public class DoctorProfitJobs {


    private final HostLeader hostLeader;
    @RpcConsumer(timeout = "6000000")
    private DoctorProfitMaterOrPigWriteServer doctorProfitMaterOrPigWriteServer;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @Autowired
    public DoctorProfitJobs(HostLeader hostLeader) {
        this.hostLeader = hostLeader;
    }

    /**
     * 猪场利润的计算
     * 每天凌晨1点统计昨天的数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0 */1 * * * ?")
    @RequestMapping(value = "/profit", method = RequestMethod.GET)
    public void profitReport() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("daily profit job start, now is:{}", DateUtil.toDateTimeString(new Date()));
            List<Long> farmIds = getAllFarmIds();
            doctorProfitMaterOrPigWriteServer.insterDoctorProfitMaterialOrPig(farmIds);
            log.info("daily profit job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily profit job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }
    private List<Long> getAllFarmIds() {
        return RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }
}
