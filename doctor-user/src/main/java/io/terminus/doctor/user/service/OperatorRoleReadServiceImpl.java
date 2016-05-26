package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.OperatorRoleDao;
import io.terminus.doctor.user.model.OperatorRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Effet
 */
@Slf4j
@Service
public class OperatorRoleReadServiceImpl implements OperatorRoleReadService {

    private final OperatorRoleDao operatorRoleDao;

    @Autowired
    public OperatorRoleReadServiceImpl(OperatorRoleDao operatorRoleDao) {
        this.operatorRoleDao = operatorRoleDao;
    }

    @Override
    public Response<OperatorRole> findById(Long id) {
        try {
            return Response.ok(operatorRoleDao.findById(id));
        } catch (Exception e) {
            log.error("find operator role by id={} failed, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("operator.role.find.fail");
        }
    }

    @Override
    public Response<List<OperatorRole>> findByIds(List<Long> ids) {
        try {
            return Response.ok(operatorRoleDao.findByIds(ids));
        } catch (Exception e) {
            log.error("find operator role by ids={} failed, cause:{}",
                    ids, Throwables.getStackTraceAsString(e));
            return Response.fail("operator.role.find.fail");
        }
    }

    @Override
    public Response<List<OperatorRole>> findByStatus(String appKey, Integer status) {
        try {
            return Response.ok(operatorRoleDao.findByStatus(appKey, status));
        } catch (Exception e) {
            log.error("find operator roles by status={} failed, cause:{}",
                    status, Throwables.getStackTraceAsString(e));
            return Response.fail("operator.role.find.fail");
        }
    }

    @Override
    public Response<Paging<OperatorRole>> pagination(String appKey, Integer status, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            OperatorRole criteria = new OperatorRole();
            criteria.setAppKey(appKey);
            criteria.setStatus(status);
            return Response.ok(operatorRoleDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging operator roles failed, status={}, pageNo={}, size={}, cause:{}",
                    status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("operator.role.paging.fail");
        }
    }
}
