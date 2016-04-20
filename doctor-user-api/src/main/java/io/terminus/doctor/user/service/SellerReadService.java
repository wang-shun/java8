package io.terminus.doctor.user.service;

import com.google.common.base.Optional;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.Seller;
import io.terminus.doctor.user.model.SubSeller;

/**
 * 商家读服务
 *
 * @author Effet
 */
public interface SellerReadService {

    /**
     * 通过主键查商家信息
     *
     * @param id 商家表主键 ID
     * @return 商家信息
     */
    Response<Seller> findSellerById(Long id);

    /**
     * 通过用户 ID 查询商家信息
     *
     * @param userId 用户 ID
     * @return 商家信息
     */
    Response<Optional<Seller>> findSellerByUserId(Long userId);

    /**
     * 分页商家信息
     *
     * @param userId 商家用户 ID
     * @param shopId 商家店铺 ID ({@code null} 表示未创建店铺)
     * @param status 商家状态
     * @param pageNo 页码
     * @param size   查询数量
     * @return 商家分页
     */
    Response<Paging<Seller>> sellerPagination(Long userId, Long shopId, Integer status, Integer pageNo, Integer size);

    /**
     * 通过主键查商家子账户
     *
     * @param id 子账号关联表主键
     * @return 子账号信息
     */
    Response<SubSeller> findSubSellerById(Long id);

    /**
     * 通过店铺 ID 和 子账户用户 ID 查询子账号关联
     *
     * @param shopId 店铺 ID
     * @param userId 子账户用户 ID
     * @return 子账号信息
     */
    Response<Optional<SubSeller>> findSubSellerByShopIdAndUserId(Long shopId, Long userId);

    /**
     * 分页子账户信息
     *
     * @param shopId 店铺 ID
     * @param status 子账户绑定状态
     * @param pageNo 页号
     * @param size   查询数量
     * @return 子账户分页
     */
    Response<Paging<SubSeller>> subSellerPagination(Long shopId, Integer status, Integer pageNo, Integer size);
}
