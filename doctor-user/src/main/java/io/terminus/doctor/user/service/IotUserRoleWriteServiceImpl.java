package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.IotRoleDao;
import io.terminus.doctor.user.dao.IotUserRoleDao;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.doctor.user.model.IotUserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Created by xjn on 17/10/11.
 */
@Slf4j
@Service
@RpcProvider
public class IotUserRoleWriteServiceImpl implements IotUserRoleWriteService {
    private final IotUserRoleDao iotUserRoleDao;
    private final IotRoleDao iotRoleDao;

    public IotUserRoleWriteServiceImpl(IotUserRoleDao iotUserRoleDao, IotRoleDao iotRoleDao) {
        this.iotUserRoleDao = iotUserRoleDao;
        this.iotRoleDao = iotRoleDao;
    }

    @Override
    public Response<Boolean> createIotRole(IotRole iotRole) {
        try {
            return Response.ok(iotRoleDao.create(iotRole));
        } catch (Exception e) {
            log.error("create iot role failed, iotRole:{}, cause:{}",
                    iotRole, Throwables.getStackTraceAsString(e));
            return Response.fail("create.iot.role.failed");
        }
    }

    @Override
    public Response<Boolean> createIotUserRole(IotUserRole iotUserRole) {
        try {
            return Response.ok(iotUserRoleDao.create(iotUserRole));
        } catch (Exception e) {
            log.error("create.iot.role.failed,iotUserRole:{}, cause:{}",
                    iotUserRole, Throwables.getStackTraceAsString(e));
            return Response.fail("create.iot.user.role.failed");
        }
    }
}
