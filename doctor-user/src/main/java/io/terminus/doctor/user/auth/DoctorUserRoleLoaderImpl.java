package io.terminus.doctor.user.auth;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.user.dao.OperatorDao;
import io.terminus.doctor.user.dao.SellerDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.SubSellerDao;
import io.terminus.doctor.user.model.*;
import io.terminus.doctor.user.service.DoctorUserRoleLoader;
import io.terminus.parana.common.utils.Iters;
import io.terminus.parana.user.auth.UserRoleLoader;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.terminus.doctor.common.util.UserRoleUtil.isAdmin;
import static io.terminus.doctor.common.util.UserRoleUtil.isNormal;
import static io.terminus.doctor.common.util.UserRoleUtil.isOperator;
import static io.terminus.doctor.common.util.UserRoleUtil.isPrimary;
import static io.terminus.doctor.common.util.UserRoleUtil.isSub;

/**
 * @author Effet
 */
@Slf4j
@Service
public class DoctorUserRoleLoaderImpl implements UserRoleLoader {

    private final UserDao userDao;

    private final SellerDao sellerDao;

    private final SubSellerDao subSellerDao;

    private final OperatorDao operatorDao;

    private final SubDao subDao;


    @Autowired
    public DoctorUserRoleLoaderImpl(UserDao userDao, SellerDao sellerDao, SubSellerDao subSellerDao, OperatorDao operatorDao, SubDao subDao) {
        this.userDao = userDao;
        this.sellerDao = sellerDao;
        this.subSellerDao = subSellerDao;
        this.operatorDao = operatorDao;
        this.subDao = subDao;
    }


    @Override
    public Response<List<String>> hardLoadRoles(Long userId) {
        try {
            if (userId == null) {
                log.warn("hard load roles failed, userId=null");
                return Response.fail("user.id.null");
            }
            User user = userDao.findById(userId);
            if (user == null) {
                log.warn("user(id={}) is not exist, no roles found", userId);
                return Response.fail("user.not.found");
            }
            Set<String> roleBuilder = new HashSet<>();

            forAdmin(user, roleBuilder);
            forOperator(user, roleBuilder);
            forNormal(user, roleBuilder);
            forPrimary(user, roleBuilder);
            forSub(user, roleBuilder);

            Set<String> originRoles = new HashSet<>();
            if (user.getRoles() != null) {
                originRoles.addAll(user.getRoles());
            }

            List<String> result = new ArrayList<>(roleBuilder);

            if (!roleBuilder.equals(originRoles)) {
                userDao.updateRoles(userId, result);
            }

            return Response.ok(result);
        } catch (Exception e) {
            log.error("hard load roles failed, userId={}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("user.role.load.fail");
        }
    }

    protected void forAdmin(User user, Collection<String> mutableRoles) {
        if (user == null || !isAdmin(user.getType())) {
            return;
        }
        mutableRoles.add("ADMIN");
        mutableRoles.add("ADMIN(OWNER)");
    }

    protected void forOperator(User user, Collection<String> mutableRoles) {
        if (user == null || !isOperator(user.getType())) {
            return;
        }
        mutableRoles.add("ADMIN");
        Operator operator = operatorDao.findByUserId(user.getId());
        if (operator != null) {
            if (operator.isActive() && operator.getRoleId() != null) {
                mutableRoles.add(String.format("ADMIN(SUB(%s))", operator.getRoleId()));
            }
        }
    }

    protected void forPrimary(User user, Collection<String> mutableRoles) {
        if (user == null || !isPrimary(user.getType())) {
            return;
        }
        mutableRoles.add("PRIMARY");
        mutableRoles.add("PRIMARY(OWNER)");
    }

    protected void forSub(User user, Collection<String> mutableRoles) {
        if (user == null || !isSub(user.getType())) {
            return;
        }
        mutableRoles.add("SUB");
        Sub sub = subDao.findByUserId(user.getId());
        if (sub != null) {
            if (sub.isActive() && sub.getRoleId() != null) {
                mutableRoles.add(String.format("SUB(SUB(%s))", sub.getRoleId()));
            }
        }
    }

    protected void forNormal(User user, Collection<String> mutableRoles) {
        if (user == null || !isNormal(user.getType())) {
            return;
        }
        // for buyer
        if (user.getRoles() != null) {
            boolean isBuyer = false;
            for (String role : user.getRoles()) {
                if (role.startsWith("BUYER")) {
                    mutableRoles.add(role);
                    isBuyer = true;
                }
            }
            if (isBuyer) {
                mutableRoles.add("BUYER");
            }
        }
        // for seller
        boolean isSeller = false;
        Seller seller = sellerDao.findByUserId(user.getId());
        if (seller != null) {
            if (seller.isActive() && seller.getShopId() != null) {
                String role = String.format("SELLER(SHOP(%s),OWNER)", seller.getShopId());
                mutableRoles.add(role);
            }
            isSeller = true;
        }
        List<SubSeller> auths = subSellerDao.findByUserId(user.getId());
        for (SubSeller auth : auths) {
            List<Long> roleIds = Lists.newArrayList();
            for (SubSeller.SubSellerRole subSellerRole : Iters.nullToEmpty(auth.getRoles())) {
                roleIds.add(subSellerRole.getId());
                isSeller = true;
            }
            if (!roleIds.isEmpty()) {
                String role = String.format("SELLER(SHOP(%s),SUB(%s))",
                        auth.getShopId(), Joiners.COMMA.join(roleIds));
                mutableRoles.add(role);
            }
        }
        if (isSeller) {
            mutableRoles.add("SELLER");
        }
    }
}
