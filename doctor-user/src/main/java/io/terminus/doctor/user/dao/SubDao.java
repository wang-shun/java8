package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.user.model.Sub;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 多条件筛选, 相当于分页查询去掉了分页参数
     * @param criteria
     * @param limit 限制数量, 可为空
     * @return
     */
    public List<Sub> findByConditions(Map<String, Object> criteria, Integer limit){
        criteria.put("limit", limit);
        return getSqlSession().selectList(sqlId("findByConditions"), ImmutableMap.copyOf(Params.filterNullOrEmpty(criteria)));
    }
}
