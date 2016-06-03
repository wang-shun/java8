package io.terminus.doctor.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserType;


import java.util.List;

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
        return user != null && isAdmin(user.getType());
    }

    public static boolean isAdmin(Integer type) {
        return type != null && type == UserType.ADMIN.value();
    }

    public static boolean isOperator(BaseUser user) {
        return isOperator(user.getType());
    }

    public static boolean isOperator(Integer type) {
        return type != null && type == UserType.OPERATOR.value();
    }

    public static boolean isNormal(BaseUser user) {
        return isNormal(user.getType());
    }

    public static boolean isNormal(Integer type) {
        return type != null && type == UserType.NORMAL.value();
    }

    public static boolean isSeller(BaseUser user) {
        return isSeller(user.getType(), user.getRoles());
    }

    public static boolean isSeller(Integer type, List<String> roles) {
        return isNormal(type) && roles != null && roles.contains(UserRole.SELLER.name());
    }

    public static boolean isBuyer(BaseUser user) {
        return isBuyer(user.getType(), user.getRoles());
    }

    public static boolean isBuyer(Integer type, List<String> roles) {
        return isNormal(type) && roles != null && roles.contains(UserRole.BUYER.name());
    }

    public static boolean isSub(BaseUser user) {
        return isSub(user.getType());
    }

    public static boolean isSub(Integer type) {
        return type != null && type == UserType.FARM_SUB.value();
    }

    public static boolean isPrimary(BaseUser user) {
        return isPrimary(user.getType());
    }

    public static boolean isPrimary(Integer type) {
        return type != null && type == UserType.FARM_ADMIN_PRIMARY.value();
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
