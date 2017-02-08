package io.terminus.doctor.web.admin.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.service.DoctorWareHouseTypeWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private DoctorWareHouseTypeWriteService doctorWareHouseTypeWriteService;

    @RpcConsumer
    private DoctorUserReadService doctorUserReadService;

    /**
     * 初始化猪舍
     * @param farm 猪场
     * @param userId 用户id
     * @return 是否成功
     */
    public Response<Boolean> initBarns(DoctorFarm farm, Long userId) {
        try {
            Response<User> response = doctorUserReadService.findById(userId);
            String userName = RespHelper.orServEx(response).getName();
            doctorWareHouseTypeWriteService.initDoctorWareHouseType(farm.getId(), farm.getName(), userId, userName);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("init barns failed, farm:{}, userId:{} cause:{}",
                    farm, userId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.init.fail");
        }
    }
}
