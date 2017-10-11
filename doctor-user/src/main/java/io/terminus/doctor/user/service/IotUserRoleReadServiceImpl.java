package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.IotUserRoleDao;
import io.terminus.doctor.user.dto.IotUserRoleInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xjn on 17/10/11.
 */
@Slf4j
@Service
@RpcProvider
public class IotUserRoleReadServiceImpl implements IotUserRoleReadService{
    private final IotUserRoleDao iotUserRoleDao;

    @Autowired
    public IotUserRoleReadServiceImpl(IotUserRoleDao iotUserRoleDao) {
        this.iotUserRoleDao = iotUserRoleDao;
    }

    @Override
    public Response<Paging<IotUserRoleInfo>> paging(String realName, String iotRoleName) {
        try {
        } catch (Exception e) {
            log.error(",cause:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }
}
