package io.terminus.doctor.user.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.user.dao.IotRoleDao;
import io.terminus.doctor.user.dao.IotUserRoleDao;
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
}
