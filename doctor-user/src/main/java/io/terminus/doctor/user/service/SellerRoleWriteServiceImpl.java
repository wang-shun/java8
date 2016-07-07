package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.SellerRoleDao;
import io.terminus.doctor.user.model.SellerRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Effet
 */
@Slf4j
@Service
@RpcProvider
public class SellerRoleWriteServiceImpl implements SellerRoleWriteService {

    private final SellerRoleDao sellerRoleDao;

    @Autowired
    public SellerRoleWriteServiceImpl(SellerRoleDao sellerRoleDao) {
        this.sellerRoleDao = sellerRoleDao;
    }

    @Override
    public Response<Long> createRole(SellerRole sellerRole) {
        try {
            sellerRoleDao.create(sellerRole);
            return Response.ok(sellerRole.getId());
        } catch (Exception e) {
            log.error("create role failed, sellerRole={}, cause:{}",
                    sellerRole, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.role.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateRole(SellerRole sellerRole) {
        try {
            return Response.ok(sellerRoleDao.update(sellerRole));
        } catch (Exception e) {
            log.error("update role failed, sellerRole={}, cause:{}",
                    sellerRole, Throwables.getStackTraceAsString(e));
            return Response.fail("seller.role.update.fail");
        }
    }
}
