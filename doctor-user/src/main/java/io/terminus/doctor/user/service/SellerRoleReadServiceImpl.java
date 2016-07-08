package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.SellerRoleDao;
import io.terminus.doctor.user.model.SellerRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Effet
 */
@Slf4j
@Service
@RpcProvider
public class SellerRoleReadServiceImpl implements SellerRoleReadService {

    private final SellerRoleDao sellerRoleDao;

    @Autowired
    public SellerRoleReadServiceImpl(SellerRoleDao sellerRoleDao) {
        this.sellerRoleDao = sellerRoleDao;
    }

    @Override
    public Response<SellerRole> findById(Long id) {
        try {
            return Response.ok(sellerRoleDao.findById(id));
        } catch (Exception e) {
            log.error("find seller role by id={} failed, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.role.find.fail");
        }
    }

    @Override
    public Response<List<SellerRole>> findByIds(List<Long> ids) {
        try {
            return Response.ok(sellerRoleDao.findByIds(ids));
        } catch (Exception e) {
            log.error("find seller role by ids={} failed, cause:{}",
                    ids, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.role.find.fail");
        }
    }

    @Override
    public Response<List<SellerRole>> findByShopIdAndStatus(String appKey, Long shopId, Integer status) {
        try {
            return Response.ok(sellerRoleDao.findByShopIdAndStatus(appKey, shopId, status));
        } catch (Exception e) {
            log.error("find seller roles by shopId={} failed, cause:{}",
                    shopId, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.role.find.fail");
        }
    }

    @Override
    public Response<Paging<SellerRole>> pagination(String appKey, Long shopId, Integer status, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            SellerRole criteria = new SellerRole();
            criteria.setAppKey(appKey);
            criteria.setShopId(shopId);
            criteria.setStatus(status);
            return Response.ok(sellerRoleDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging seller roles failed, shopId={}, status={}, pageNo={}, size={}, cause:{}",
                    shopId, status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.role.paging.fail");
        }
    }
}
