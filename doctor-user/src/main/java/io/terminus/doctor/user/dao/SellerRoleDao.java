package io.terminus.doctor.user.dao;

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
     * @param shopId 用户id
     * @return 用户信息
     */
    public List<SellerRole> findByShopId(Long shopId){
        return getSqlSession().selectList(sqlId("findByShopId"), shopId);
    }

}
