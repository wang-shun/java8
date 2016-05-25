package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.PrimaryUser;
import org.springframework.stereotype.Repository;

/**
 * @author Effet
 */
@Repository
public class PrimaryUserDao extends MyBatisDao<PrimaryUser> {

    public PrimaryUser findByUserId(Long userId) {
        return getSqlSession().selectOne(sqlId("findByUserId"), userId);
    }
}
