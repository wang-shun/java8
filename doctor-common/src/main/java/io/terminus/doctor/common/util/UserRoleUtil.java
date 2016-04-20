package io.terminus.doctor.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserType;

import java.util.List;
import java.util.Objects;

/**
 * 用户角色工具类
 *
 * @author Effet
 */
public class UserRoleUtil {

    /**
     * 判断用户是否为 ADMIN
     *
     * @param user 用户信息
     * @return 是否为 ADMIN
     */
    public static boolean isAdmin(BaseUser user) {
        return Objects.equals(user.getType(), UserType.ADMIN.value());
    }

    public static boolean isOperator(BaseUser user) {
        return isAdmin(user) || Objects.equals(user.getType(), UserType.OPERATOR.value());
    }

    public static boolean isSeller(List<String> roles) {
        return roles.contains(UserRole.SELLER.name());
    }

    public static boolean isSeller(BaseUser user) {
        return isSeller(user.getRoles());
    }

    public static boolean isBuyer(List<String> roles) {
        return roles.contains(UserRole.BUYER.name());
    }

    public static boolean isBuyer(BaseUser user) {
        return isBuyer(user.getRoles());
    }

    // TODO: 只考虑最规范的语法
    public static List<String> roleConsFrom(String str) {
        if (Strings.isNullOrEmpty(str)) {
            throw new RuntimeException("invalid role");
        }
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == '(') {
                String inner = str.substring(i + 1, str.length() - 1);
                result.add(str.substring(0, i));
                result.addAll(roleListHelper(inner));
                return result;
            }
        }
        result.add(str);
        return result;
    }

    private static List<String> roleListHelper(String list) {
        if (Strings.isNullOrEmpty(list)) {
            return Lists.newArrayList();
        }
        for (int i = 0; i < list.length(); ++i) {
            if (list.charAt(i) == ',') {
                List<String> result = Lists.newArrayList();
                result.add(list.substring(0, i));
                result.addAll(roleListHelper(list.substring(i + 1)));
                return result;
            }
        }
        return Lists.newArrayList(list);
    }
}
