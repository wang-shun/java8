package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.IotRoleDao;
import io.terminus.doctor.user.dao.IotUserRoleDao;
import io.terminus.doctor.user.dto.IotUserRoleInfo;
import io.terminus.doctor.user.model.IotRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xjn on 17/10/11.
 */
@Slf4j
@Service
@RpcProvider
public class IotUserRoleReadServiceImpl implements IotUserRoleReadService{
    private final IotUserRoleDao iotUserRoleDao;
    private final IotRoleDao iotRoleDao;

    @Autowired
    public IotUserRoleReadServiceImpl(IotUserRoleDao iotUserRoleDao, IotRoleDao iotRoleDao) {
        this.iotUserRoleDao = iotUserRoleDao;
        this.iotRoleDao = iotRoleDao;
    }

    @Override
    public Response<Paging<IotUserRoleInfo>> paging(String realName, String iotRoleName ) {
        try {
        } catch (Exception e) {
            log.error(",cause:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    @Override
    public Response<List<IotRole>> listEffected() {
        try {
            return Response.ok(iotRoleDao.listEffected());
        } catch (Exception e) {
            log.error("list all iot role failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("list.all.iot.role.failed");
        }
    }
}
