package io.terminus.doctor.user.service;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.SellerDao;
import io.terminus.doctor.user.dao.SubSellerDao;
import io.terminus.doctor.user.model.Seller;
import io.terminus.doctor.user.model.SubSeller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Effet
 */
@Slf4j
@Service
@RpcProvider
public class SellerReadServiceImpl implements SellerReadService {

    private final SellerDao sellerDao;

    private final SubSellerDao subSellerDao;

    @Autowired
    public SellerReadServiceImpl(SellerDao sellerDao, SubSellerDao subSellerDao) {
        this.sellerDao = sellerDao;
        this.subSellerDao = subSellerDao;
    }

    @Override
    public Response<Seller> findSellerById(Long id) {
        try {
            Seller seller = sellerDao.findById(id);
            if (seller == null) {
                log.warn("seller not found, id={}", id);
                return Response.fail("seller.not.found");
            }
            return Response.ok(seller);
        } catch (Exception e) {
            log.error("find seller failed, id={}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.find.fail");
        }
    }

    @Override
    public Response<Optional<Seller>> findSellerByUserId(Long userId) {
        try {
            Seller seller = sellerDao.findByUserId(userId);
            return Response.ok(Optional.fromNullable(seller));
        } catch (Exception e) {
            log.error("find seller by userId={} failed, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.find.fail");
        }
    }

    @Override
    public Response<Paging<Seller>> sellerPagination(Long userId, Long shopId, Integer status, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            Seller criteria = new Seller();
            criteria.setUserId(userId);
            criteria.setShopId(shopId);
            criteria.setStatus(status);
            return Response.ok(sellerDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging seller failed, userId={}, shopId={}, status={}, pageNo={}, size={}, cause:{}",
                    userId, shopId, status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.paging.fail");
        }
    }

    @Override
    public Response<SubSeller> findSubSellerById(Long id) {
        try {
            SubSeller subSeller = subSellerDao.findById(id);
            if (subSeller == null) {
                log.warn("sub seller not found, id={}", id);
                return Response.fail("seller.sub.not.found");
            }
            return Response.ok(subSeller);
        } catch (Exception e) {
            log.error("find sub seller by id={} failed, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.sub.find.fail");
        }
    }

    @Override
    public Response<Optional<SubSeller>> findSubSellerByShopIdAndUserId(Long shopId, Long userId) {
        try {
            SubSeller subSeller = subSellerDao.findByShopIdAndUserId(shopId, userId);
            return Response.ok(Optional.fromNullable(subSeller));
        } catch (Exception e) {
            log.error("find sub seller by shopId={} and userId={} failed, cause:{}",
                    shopId, userId, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.sub.find.fail");
        }
    }

    @Override
    public Response<Paging<SubSeller>> subSellerPagination(Long shopId, Integer status, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            SubSeller criteria = new SubSeller();
            criteria.setShopId(shopId);
            criteria.setStatus(status);
            return Response.ok(subSellerDao.paging(page.getOffset(), page.getLimit()));
        } catch (Exception e) {
            log.error("paging sub seller failed, shopId={}, status={}, pageNo={}, size={}, cause:{}",
                    shopId, status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.sub.paging.fail");
        }
    }
}
