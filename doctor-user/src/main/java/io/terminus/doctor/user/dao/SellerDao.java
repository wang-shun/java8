package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.Seller;
import org.springframework.stereotype.Repository;

/**
 * @author Effet
 */
@Repository
public class SellerDao extends MyBatisDao<Seller> {

    public Seller findByUserId(Long userId) {
        return getSqlSession().selectOne(sqlId("findByUserId"), userId);
    }
}
