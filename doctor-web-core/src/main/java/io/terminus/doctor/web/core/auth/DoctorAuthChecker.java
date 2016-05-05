package io.terminus.doctor.web.core.auth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.util.UserRoleUtil;
import io.terminus.doctor.user.model.OperatorRole;
import io.terminus.doctor.user.model.SellerRole;
import io.terminus.doctor.user.service.OperatorRoleReadService;
import io.terminus.doctor.user.service.SellerRoleReadService;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.parana.auth.AuthChecker;
import io.terminus.parana.auth.CompiledTree;
import io.terminus.parana.auth.Req;
import io.terminus.parana.user.auth.UserRoleLoader;
import io.terminus.parana.auth.parser.ParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Effet
 */
@Slf4j
@Component
public class DoctorAuthChecker {

    private final AuthLoader authLoader;

    private final SellerRoleReadService sellerRoleReadService;

    private final OperatorRoleReadService operatorRoleReadService;

    private final LoadingCache<Long, List<String>> userRoleCache;

    @Autowired
    public DoctorAuthChecker(final UserRoleLoader userRoleLoader,
                             AuthLoader authLoader,
                             SellerRoleReadService sellerRoleReadService,
                             OperatorRoleReadService operatorRoleReadService) {
        this.authLoader = authLoader;
        this.sellerRoleReadService = sellerRoleReadService;
        this.operatorRoleReadService = operatorRoleReadService;

        userRoleCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<Long, List<String>>() {
            @Override
            public List<String> load(Long userId) throws Exception {
                return userRoleLoader.hardLoadRoles(userId);
            }
        });
    }

    public boolean forNotLogin(Req req) {
        ParseResult result = authLoader.getTree(ThreadVars.getApp());
        AuthChecker.Holder holder = new AuthChecker(result.getTrees())
                .holder(Lists.<CompiledTree>newArrayList(), Lists.newArrayList("GLOBAL"));
        return holder.checkReq(req);
    }

    public boolean forLogin(Req req, BaseUser user) {
        ParseResult result = authLoader.getTree(ThreadVars.getApp());
        AuthChecker checker = new AuthChecker(result.getTrees());

        List<String> roles = userRoleCache.getUnchecked(user.getId());

        // admin 特殊处理用户
        if (user.getType() != UserType.NORMAL.value()) {
            if (user.getType() == UserType.ADMIN.value()) {
                return true; // TODO: 暂时放开权限
//                return checkInAll("ADMIN", result, req);
            }
            if (user.getType() == UserType.OPERATOR.value()) {
                return checkForOperator(checker, roles, null, req);
            }
            if (user.getType() == UserType.SITE_OWNER.value()) {
                return checkInAll(checker, "SITE_OWNER", null, req);
            }
            return false;
        }

        // 角色暂时只支持这么多
        // 1. 买家个人
        if (checkForBuyer(checker, roles, null, req)) {
            return true;
        }
        // 2. 卖家个人
        if (checkForSeller(checker, roles, null, req)) {
            return true;
        }

        // 登录用户权限
        return checkInAll(checker, "LOGIN", null, req);
    }

    public boolean forNotLogin_key(String key) {
        ParseResult result = authLoader.getTree(ThreadVars.getApp());
        AuthChecker checker = new AuthChecker(result.getTrees());
        return checkInAll(checker, "GLOBAL", key, null);
    }

    public boolean forLogin_key(String key, BaseUser user) {
        ParseResult result = authLoader.getTree(ThreadVars.getApp());
        AuthChecker checker = new AuthChecker(result.getTrees());

        List<String> roles = userRoleCache.getUnchecked(user.getId());

        // admin 特殊处理用户
        if (user.getType() != UserType.NORMAL.value()) {
            if (user.getType() == UserType.ADMIN.value()) {
                return true; // TODO: 暂时放开权限
//                return checkInAll("ADMIN", result, req);
            }
            if (user.getType() == UserType.OPERATOR.value()) {
                return checkForOperator(checker, roles, key, null);
            }
            if (user.getType() == UserType.SITE_OWNER.value()) {
                return checkInAll(checker, "SITE_OWNER", key, null);
            }
            return false;
        }

        // 暂时只支持这么多
        // 1. 买家个人
        if (checkForBuyer(checker, roles, key, null)) {
            return true;
        }
        // 2. 卖家个人
        if (checkForSeller(checker, roles, key, null)) {
            return true;
        }

        // 登录用户权限
        return checkInAll(checker, "LOGIN", key, null);
    }

    private boolean checkForBuyer(AuthChecker checker, List<String> roles, String key, Req req) {
        int maxLevel = findMaxLevel(roles, UserRole.BUYER.name());
        // 没有买家权限
        if (maxLevel < 0) {
            return false;
        }
        // master 暂时和 owner 同级
        if (maxLevel == OWNER || maxLevel == MASTER) {
            return true; // TODO: 暂时放开权限
//            return checkInAll(UserRole.BUYER.name(), forest, req);
        }
        // 买家个人现在没有子账户
        return false;
    }

    private boolean checkForSeller(AuthChecker checker, List<String> roles, String key, Req req) {
        int maxLevel = findMaxLevel(roles, UserRole.SELLER.name());
        // 没有卖家权限
        if (maxLevel < 0) {
            return false;
        }
        // master 暂时和 owner 同级
        if (maxLevel == OWNER || maxLevel == MASTER) {
            return true; // TODO: 暂时放开权限
//            return checkInAll(UserRole.SELLER.name(), forest, req);
        }
        // 卖家现在只支持个人卖家
        List<Long> roleIds = Lists.newArrayList();
        for (String role : roles) {
            List<String> richRole = UserRoleUtil.roleConsFrom(role);
            if (richRole.size() > 1 && Objects.equals(richRole.get(0), UserRole.SELLER.name())) {
                for (String inner : richRole.subList(1, richRole.size())) {
                    List<String> subRole = UserRoleUtil.roleConsFrom(inner);
                    if (subRole.size() > 1 && Objects.equals(subRole.get(0), "SUB")) {
                        Long roleId = Long.parseLong(subRole.get(1));
                        roleIds.add(roleId);
                    }
                }
            }
        }
        List<SellerRole> sellerRoles = RespHelper.orServEx(sellerRoleReadService.findByIds(roleIds));

        List<CompiledTree> allows = Lists.newArrayList();
        for (SellerRole sellerRole : sellerRoles) {
            if (sellerRole.getStatus() > 0) {
                allows.addAll(sellerRole.getAllow());
            }
        }
        return checkInHaving(checker, allows, key, req);
    }

    private boolean checkForOperator(AuthChecker checker, List<String> roles, String key, Req req) {
        List<Long> roleIds = Lists.newArrayList();
        for (String role : roles) {
            List<String> richRole = UserRoleUtil.roleConsFrom(role);
            if (richRole.size() > 1 && Objects.equals(richRole.get(0), "ADMIN")) {
                for (String inner : richRole.subList(1, richRole.size())) {
                    List<String> subRole = UserRoleUtil.roleConsFrom(inner);
                    if (subRole.size() > 1 && Objects.equals(subRole.get(0), "SUB")) {
                        Long roleId = Long.parseLong(subRole.get(1));
                        roleIds.add(roleId);
                    }
                }
            }
        }
        // operator 只支持单个角色
        List<OperatorRole> operatorRoles = RespHelper.orServEx(operatorRoleReadService.findByIds(roleIds));

        List<CompiledTree> allows = Lists.newArrayList();
        for (OperatorRole operatorRole : operatorRoles) {
            if (operatorRole.getStatus() > 0) {
                allows.addAll(operatorRole.getAllow());
            }
        }
        return checkInHaving(checker, allows, key, req);
    }

    private int findMaxLevel(List<String> roles, String userRole) {
        int maxLevel = -1;
        for (String role : roles) {
            List<String> richRole = UserRoleUtil.roleConsFrom(role);
            if (richRole.size() > 1 && Objects.equals(richRole.get(0), userRole)) {
                for (String inner : richRole.subList(1, richRole.size())) {
                    List<String> subRole = UserRoleUtil.roleConsFrom(inner);
                    if (subRole.size() >= 1 && Objects.equals(subRole.get(0), "OWNER")) {
                        maxLevel = OWNER;
                    } else if (subRole.size() >= 1 && Objects.equals(subRole.get(0), "SUB")) {
                        if (maxLevel == -1) {
                            maxLevel = SUB;
                        }
                    }
                }
            }
        }
        return maxLevel;
    }

    private boolean checkInAll(AuthChecker checker, String scope, String key, Req req) {
        AuthChecker.Holder holder = checker.holder(Lists.<CompiledTree>newArrayList(), Lists.newArrayList(scope));
        if (key != null) {
            return holder.checkKey(key);
        }
        return holder.checkReq(req);
    }

    private boolean checkInHaving(AuthChecker checker, List<CompiledTree> allows, String key, Req req) {
        AuthChecker.Holder holder = checker.holder(allows, Lists.<String>newArrayList());
        if (key != null) {
            return holder.checkKey(key);
        }
        return holder.checkReq(req);
    }

    int OWNER = 1;
    int MASTER = 2;
    int SUB = 3;
}
