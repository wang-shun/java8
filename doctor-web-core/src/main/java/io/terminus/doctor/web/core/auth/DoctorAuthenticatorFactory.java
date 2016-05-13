package io.terminus.doctor.web.core.auth;

import com.google.common.collect.Lists;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.util.UserRoleUtil;
import io.terminus.doctor.user.model.OperatorRole;
import io.terminus.doctor.user.model.SellerRole;
import io.terminus.doctor.user.service.OperatorRoleReadService;
import io.terminus.doctor.user.service.SellerRoleReadService;
import io.terminus.parana.auth.CompiledTree;
import io.terminus.parana.auth.web.AuthLoader;
import io.terminus.parana.auth.web.component.DefaultAuthenticatorFactory;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.auth.UserRoleLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Effet
 */
@Slf4j
@Component
public class DoctorAuthenticatorFactory extends DefaultAuthenticatorFactory {

    private final SellerRoleReadService sellerRoleReadService;

    private final OperatorRoleReadService operatorRoleReadService;

    @Autowired
    public DoctorAuthenticatorFactory(UserRoleLoader userRoleLoader,
                                      AuthLoader authLoader,
                                      SellerRoleReadService sellerRoleReadService,
                                      OperatorRoleReadService operatorRoleReadService) {
        super(userRoleLoader, authLoader);
        this.sellerRoleReadService = sellerRoleReadService;
        this.operatorRoleReadService = operatorRoleReadService;
    }

    @Override
    protected void forLogin(List<CompiledTree> mutableAllows, List<String> mutableScopes, BaseUser user) {
        mutableScopes.add("GLOBAL");
        mutableScopes.add("LOGIN");

        Integer userType = user.getType();

        if (userType == null) {
            return;
        }

        if (userType != UserType.NORMAL.value()) {
            if (userType == UserType.ADMIN.value()) {
                mutableScopes.add("ADMIN");
            }
            if (userType == UserType.OPERATOR.value()) {
                List<String> roles = userRoleCache.getUnchecked(user.getId());
                forOperator(mutableAllows, mutableScopes, roles);
            }
            return;
        }

        List<String> roles = userRoleCache.getUnchecked(user.getId());

        // 买家权限
        forBuyer(mutableAllows, mutableScopes, roles);

        // 卖家权限
        forSeller(mutableAllows, mutableScopes, roles);
    }

    private void forBuyer(List<CompiledTree> mutableAllows, List<String> mutableScopes, List<String> roles) {
        int maxLevel = findMaxLevel(roles, UserRole.BUYER.name());
        // 没有买家权限
        if (maxLevel < 0) {
            return;
        }
        // master 暂时和 owner 同级
        if (maxLevel == OWNER || maxLevel == MASTER) {
            mutableScopes.add("BUYER");
        }
        // 买家个人现在没有子账户
    }

    private void forSeller(List<CompiledTree> mutableAllows, List<String> mutableScopes, List<String> roles) {
        int maxLevel = findMaxLevel(roles, UserRole.SELLER.name());
        // 没有卖家权限
        if (maxLevel < 0) {
            return;
        }
        // master 暂时和 owner 同级
        if (maxLevel == OWNER || maxLevel == MASTER) {
            mutableScopes.add("SELLER");
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
        mutableAllows.addAll(allows);
    }

    private void forOperator(List<CompiledTree> mutableAllows, List<String> mutableScopes, List<String> roles) {
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
        mutableAllows.addAll(allows);
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

    int OWNER = 1;
    int MASTER = 2;
    int SUB = 3;
}
