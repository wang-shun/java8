package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.Operator;
import org.springframework.stereotype.Repository;

/**
 * @author Effet
 */
@Repository
public class OperatorDao extends MyBatisDao<Operator> {

    public Operator findByUserId(Long userId) {
        return getSqlSession().selectOne(sqlId("findByUserId"), userId);
    }
}
