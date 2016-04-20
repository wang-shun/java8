package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.SubSeller;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Effet
 */
@Repository
public class SubSellerDao extends MyBatisDao<SubSeller> {

    public List<SubSeller> findByUserId(Long userId) {
        return getSqlSession().selectList(sqlId("findByUserId"), userId);
    }

    public SubSeller findByShopIdAndUserId(Long shopId, Long userId) {
        return getSqlSession().selectOne(sqlId("findByShopIdAndUserId"),
                ImmutableMap.of("shopId", shopId, "userId", userId));
    }
}
