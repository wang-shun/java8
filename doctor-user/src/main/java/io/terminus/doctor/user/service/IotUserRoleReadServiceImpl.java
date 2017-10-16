package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.user.dao.IotRoleDao;
import io.terminus.doctor.user.dao.IotUserRoleDao;
import io.terminus.doctor.user.dto.IotUserRoleInfo;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.doctor.user.model.IotUserRole;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by xjn on 17/10/11.
 */
@Slf4j
@Service
@RpcProvider
public class IotUserRoleReadServiceImpl implements IotUserRoleReadService{
    private final IotUserRoleDao iotUserRoleDao;
    private final IotRoleDao iotRoleDao;
    private final UserProfileDao userProfileDao;
    private final UserDao userDao;

    @Autowired
    public IotUserRoleReadServiceImpl(IotUserRoleDao iotUserRoleDao, IotRoleDao iotRoleDao,
                                      UserProfileDao userProfileDao, UserDao userDao) {
        this.iotUserRoleDao = iotUserRoleDao;
        this.iotRoleDao = iotRoleDao;
        this.userProfileDao = userProfileDao;
        this.userDao = userDao;
    }

    @Override
    public Response<Paging<IotUserRoleInfo>> paging(String realName, Integer pageNo, Integer pageSize) {
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
    public Response<IotUserRoleInfo> findIotUserRoleById(Long id) {
        try {
            IotUserRole iotUserRole = iotUserRoleDao.findById(id);
            UserProfile userProfile = userProfileDao.findByUserId(iotUserRole.getUserId());
            String name;
            if (notNull(userProfile)) {
                name = userProfile.getRealName();
            } else {
                User user = userDao.findById(iotUserRole.getUserId());
                name = user.getName();
            }
            IotUserRoleInfo info = BeanMapper.map(iotUserRole, IotUserRoleInfo.class);
            info.setRealName(name);
            return Response.ok(info);
        } catch (Exception e) {
            log.error("find iot user role by id failed, id:{},cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("find.iot.user.role.by.id.failed");
        }
    }
}
