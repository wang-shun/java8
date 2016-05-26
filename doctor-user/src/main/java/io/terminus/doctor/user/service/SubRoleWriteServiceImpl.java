package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
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
}
