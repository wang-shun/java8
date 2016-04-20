package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.SellerRole;

import java.util.List;

/**
 * 商家自定义角色读服务
 *
 * @author Effet
 */
public interface SellerRoleReadService {

    /**
     * 通过 ID 查询
     *
     * @param id 主键 ID
     * @return 自定义角色
     */
    Response<SellerRole> findById(Long id);

    /**
     * 通过 IDs 批量查询
     *
     * @param ids 主键 ID 列表
     * @return 自定义角色列表
     */
    Response<List<SellerRole>> findByIds(List<Long> ids);

    /**
     * 通过店铺 ID 查询
     *
     * @param shopId 店铺 ID
     * @return 自定义角色列表
     */
    Response<List<SellerRole>> findByShopId(Long shopId);

    /**
     * 分页查询
     *
     * @param shopId 店铺 ID
     * @param status 角色状态
     * @param pageNo 页码
     * @param size   查询个数
     * @return 自定义角色分页
     */
    Response<Paging<SellerRole>> pagination(Long shopId, Integer status, Integer pageNo, Integer size);
}
