package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.SubRoleDao;
import io.terminus.doctor.user.model.SubRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by houluyao on 16/5/24.
 */
@Slf4j
@Service
@RpcProvider
public class SubRoleReadServiceImpl implements SubRoleReadService {
    private final SubRoleDao subRoleDao;

    @Autowired
    public SubRoleReadServiceImpl(SubRoleDao subRoleDao) {
        this.subRoleDao = subRoleDao;
    }

    @Override
    public Response<SubRole> findById(Long id) {
        try {
            return Response.ok(subRoleDao.findById(id));
        } catch (Exception e) {
            log.error("find sub role by id={} failed, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.find.fail");
        }
    }

    @Override
    public Response<List<SubRole>> findByIds(List<Long> ids) {
        try {
            return Response.ok(subRoleDao.findByIds(ids));
        } catch (Exception e) {
            log.error("find sub role by ids={} failed, cause:{}",
                    ids, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.find.fail");
        }
    }

    @Override
    public Response<List<SubRole>> findByUserIdAndStatus(String appKey, Long userId, Integer status) {
        try {
            return Response.ok(subRoleDao.findByUserIdAndStatus(appKey, userId, status));
        } catch (Exception e) {
            log.error("find seller roles by userId={} failed, cause:{}",
                    userId, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.find.fail");
        }
    }

    @Override
    public Response<List<SubRole>> findByFarmIdAndStatus(String appKey, Long farmId, Integer status) {
        try {
            return Response.ok(subRoleDao.findByFarmIdAndStatus(appKey, farmId, status));
        } catch (Exception e) {
            log.error("find farm roles by farmId={} failed, cause:{}",
                    farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("farm.role.find.fail");
        }
    }

    @Override
    public Response<Paging<SubRole>> pagination(String appKey, Long userId, Integer status, String roleName, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            SubRole criteria = new SubRole();
            criteria.setAppKey(appKey);
            criteria.setUserId(userId);
            criteria.setStatus(status);
            criteria.setName(roleName);
            return Response.ok(subRoleDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging seller roles failed, userId={}, status={}, pageNo={}, size={}, cause:{}",
                    userId, status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.paging.fail");
        }
    }

    @Override
    public Response<Paging<SubRole>> pagingRole(String appKey, Long farmId, Integer status, String roleName, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            SubRole criteria = new SubRole();
            criteria.setAppKey(appKey);
            criteria.setFarmId(farmId);
            criteria.setStatus(status);
            criteria.setName(roleName);
            return Response.ok(subRoleDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging seller roles failed, farmId={}, status={}, pageNo={}, size={}, cause:{}",
                    farmId, status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.paging.fail");
        }
    }
}
