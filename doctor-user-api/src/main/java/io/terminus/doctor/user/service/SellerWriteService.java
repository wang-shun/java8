package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.Seller;
import io.terminus.doctor.user.model.SubSeller;

/**
 * 卖家写服务
 *
 * @author Effet
 */
public interface SellerWriteService {

    /**
     * 创建卖家
     *
     * @param seller 卖家信息
     * @return 卖家表主键 ID
     */
    Response<Long> createSeller(Seller seller);

    /**
     * 更新卖家
     *
     * @param seller 卖家信息
     * @return 是否更新
     */
    Response<Boolean> updateSeller(Seller seller);

    /**
     * 创建卖家子账户
     *
     * @param subSeller 子账户关联信息
     * @return 关键表主键 ID
     */
    Response<Long> createSubSeller(SubSeller subSeller);

    /**
     * 更新卖家子账户
     *
     * @param subSeller 子账户信息
     * @return 是否更新
     */
    Response<Boolean> updateSubSeller(SubSeller subSeller);
}
