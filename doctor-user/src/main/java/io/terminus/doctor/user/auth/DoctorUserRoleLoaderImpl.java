package io.terminus.doctor.user.auth;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.util.UserRoleUtil;
import io.terminus.doctor.user.dao.SellerDao;
import io.terminus.doctor.user.dao.SubSellerDao;
import io.terminus.doctor.user.model.Seller;
import io.terminus.doctor.user.model.SubSeller;
import io.terminus.parana.common.utils.Iters;
import io.terminus.parana.user.auth.UserRoleLoader;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Effet
 */
@Slf4j
@Service
public class DoctorUserRoleLoaderImpl implements UserRoleLoader {

    private final UserDao userDao;

    private final SellerDao sellerDao;

    private final SubSellerDao subSellerDao;

    @Autowired
    public DoctorUserRoleLoaderImpl(UserDao userDao, SellerDao sellerDao, SubSellerDao subSellerDao) {
        this.userDao = userDao;
        this.sellerDao = sellerDao;
        this.subSellerDao = subSellerDao;
    }

    @Override
    public List<String> hardLoadRoles(Long userId) {
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                log.warn("user(id={}) is not exist, no roles found", userId);
                return Collections.emptyList();
            }
            if (user.getType() == UserType.NORMAL.value()) {
                List<String> result = Lists.newArrayList();
                for (String role : Iters.nullToEmpty(user.getRoles())) {
                    List<String> richRole = UserRoleUtil.roleConsFrom(role);
                    // 非卖家不重载
                    if (!richRole.get(0).equals(UserRole.SELLER.name())) {
                        result.add(role);
                    }
                }

                boolean isSeller = false;
                Seller seller = sellerDao.findByUserId(user.getId());
                if (seller != null) {
                    if (Objects.equals(seller.getStatus(), 1) && seller.getShopId() != null) {
                        String role = "SELLER(SHOP(" + seller.getShopId() + "),OWNER)";
                        result.add(role);
                    }
                    isSeller = true;
                }
                List<SubSeller> auths = subSellerDao.findByUserId(user.getId());
                for (SubSeller auth : auths) {
                    for (SubSeller.SubSellerRole subSellerRole : Iters.nullToEmpty(auth.getRoles())) {
                        String role = "SELLER(SHOP(" + auth.getShopId() + "),SUB(" + subSellerRole.getId() + "))";
                        result.add(role);
                        isSeller = true;
                    }
                }
                if (isSeller) {
                    result.add("SELLER");
                }

                userDao.updateRoles(userId, result);

                return result;
            }
            return Iters.nullToEmpty(user.getRoles());
        } catch (Exception e) {
            log.error("hard load roles failed, userId={}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            throw new ServiceException("role.load.fail");
        }
    }
}
