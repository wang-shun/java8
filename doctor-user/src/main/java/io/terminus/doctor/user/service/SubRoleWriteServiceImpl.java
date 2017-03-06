package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.SubRoleDao;
import io.terminus.doctor.user.model.SubRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by houluyao on 16/5/24.
 */
@Slf4j
@Service
@RpcProvider
public class SubRoleWriteServiceImpl implements SubRoleWriteService {

    private final SubRoleDao subRoleDao;

    @Autowired
    public SubRoleWriteServiceImpl(SubRoleDao subRoleDao) {
        this.subRoleDao = subRoleDao;
    }

    @Override
    public Response<Long> createRole(SubRole subRole) {
        try {
            subRoleDao.create(subRole);
            return Response.ok(subRole.getId());
        } catch (Exception e) {
            log.error("create sub role failed, subRole={}, cause:{}",
                    subRole, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateRole(SubRole subRole) {
        try {
            return Response.ok(subRoleDao.update(subRole));
        } catch (Exception e) {
            log.error("update sub role failed, subRole={}, cause:{}",
                    subRole, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.update.fail");
        }
    }

    @Override
    public Response<Boolean> initDefaultRoles(String appKey, Long userId, Long farmId){
        try {
            subRoleDao.findByUserIdAndStatus(appKey, 0L, 1).forEach(subRole -> {
                subRole.setUserId(userId);
                subRole.setFarmId(farmId);
                subRoleDao.create(subRole);
            });
            return Response.ok(true);
        } catch (Exception e) {
            log.error("init default roles fail, userId={}, cause : {}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("init.default.roles.fail");
        }
    }
}
