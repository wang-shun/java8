package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.SellerRole;

/**
 * 商家自定义角色读服务
 *
 * @author Effet
 */
public interface SellerRoleWriteService {

    /**
     * 创建自定义角色
     *
     * @param sellerRole 自定义角色
     * @return 主键 ID
     */
    Response<Long> createRole(SellerRole sellerRole);

    /**
     * 更新自定义角色
     *
     * @param sellerRole 自定义角色
     * @return 是否更新成功
     */
    Response<Boolean> updateRole(SellerRole sellerRole);
}
