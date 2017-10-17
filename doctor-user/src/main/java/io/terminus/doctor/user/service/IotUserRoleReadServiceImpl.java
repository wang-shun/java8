package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.IotRoleDao;
import io.terminus.doctor.user.dao.IotUserDao;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.doctor.user.model.IotUser;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.impl.dao.UserProfileDao;
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
    private final IotUserDao iotUserDao;
    private final IotRoleDao iotRoleDao;
    private final UserProfileDao userProfileDao;
    private final UserDao userDao;

    @Autowired
    public IotUserRoleReadServiceImpl(IotUserDao iotUserDao, IotRoleDao iotRoleDao,
                                      UserProfileDao userProfileDao, UserDao userDao) {
        this.iotUserDao = iotUserDao;
        this.iotRoleDao = iotRoleDao;
        this.userProfileDao = userProfileDao;
        this.userDao = userDao;
    }

    @Override
    public Response<Paging<IotUser>> paging(String realName, List<Integer> statuses, Integer pageNo, Integer pageSize) {
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

    @Override
    public Response<IotRole> findIotRoleById(Long id) {
        try {
            return Response.ok(iotRoleDao.findById(id));
        } catch (Exception e) {
            log.error("find iot role by id failed,id:{}, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("find.iot.role.by.id.failed");
        }
    }

    @Override
    public Response<IotUser> findIotUserRoleById(Long id) {
        try {
            return Response.ok(iotUserDao.findById(id));
        } catch (Exception e) {
            log.error("find iot user role by id failed, id:{},cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("find.iot.user.role.by.id.failed");
        }
    }
}
