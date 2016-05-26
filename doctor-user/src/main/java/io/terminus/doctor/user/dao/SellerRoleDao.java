package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.SellerRole;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by cuiwentao on 16/3/7.
 */
@Repository
public class SellerRoleDao extends MyBatisDao<SellerRole> {

    /**
     * 以shopId查询信息
     *
     * @param appKey 角色使用场景
     * @param shopId 用户id
     * @param status 角色状态
     * @return 用户信息
     */
    public List<SellerRole> findByShopIdAndStatus(String appKey, Long shopId, Integer status) {
        return getSqlSession().selectList(sqlId("findByShopIdAndStatus"),
                ImmutableMap.of("appKey", appKey, "shopId", shopId, "status", status));
    }

}
