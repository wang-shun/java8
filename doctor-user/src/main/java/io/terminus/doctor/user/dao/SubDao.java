package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.Sub;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author houly
 */
@Repository
public class SubDao extends MyBatisDao<Sub> {

    public Sub findByUserId(Long userId) {
        return getSqlSession().selectOne(sqlId("findByUserId"), userId);
    }

    public Sub findByParentUserIdAndUserId(Long parentUserId, Long userId) {
        return getSqlSession().selectOne(sqlId("findByParentUserIdAndUserId"), ImmutableMap.of("parentUserId", parentUserId, "userId", userId));
    }

    /**
     * 获取所有审核通过的子账号
     * @return
     */
    public List<Sub> findAllActiveSubs() {
        return getSqlSession().selectList(sqlId("findAllActiveSubs"));
    }
}
