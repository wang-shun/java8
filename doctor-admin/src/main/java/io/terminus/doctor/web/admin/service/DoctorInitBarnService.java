package io.terminus.doctor.web.admin.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorBarnWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static io.terminus.doctor.common.utils.RespHelper.or500;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/22
 */
@Slf4j
@Service
public class DoctorInitBarnService {

    @RpcConsumer
    private DoctorBarnWriteService doctorBarnWriteService;

    @RpcConsumer
    private DoctorBarnReadService doctorBarnReadService;

    /**
     * 初始化猪舍
     * @param farm 猪场
     * @param userId 用户id
     * @return 是否成功
     */
    public Response<Boolean> initBarns(DoctorFarm farm, Long userId) {
        try {
            or500(doctorBarnReadService.findBarnsByFarmId(0L)).forEach(barn -> {
                barn.setFarmId(farm.getId());
                barn.setFarmName(farm.getName());
                barn.setOrgId(farm.getOrgId());
                barn.setOrgName(farm.getOrgName());
                barn.setStaffId(userId);
                Long barnId = or500(doctorBarnWriteService.createBarn(barn));
                doctorBarnWriteService.publistBarnEvent(barnId);
                log.info("init barn info, barn:{}", barn);
            });
            //发事件
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("init barns failed, farm:{}, userId:{} cause:{}",
                    farm, userId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.init.fail");
        }
    }
}
