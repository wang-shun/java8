package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.OperatorRole;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Effet
 */
@Repository
public class OperatorRoleDao extends MyBatisDao<OperatorRole> {

    public List<OperatorRole> findByStatus(String appKey, Integer status) {
        return getSqlSession().selectList(sqlId("findByStatus"),
                ImmutableMap.of("appKey", appKey, "status", status));
    }
}
