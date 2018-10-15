package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.user.model.SubRole;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by houly on 16/5/24.
 */
@Repository
public class SubRoleDao extends MyBatisDao<SubRole> {

    // 得到默认的公司角色（2018-10-15）
    public SubRole getCompanyRole() {
        return this.sqlSession.selectOne(this.sqlId("getCompanyRole"));
    }

    /**
     * 以userId查询信息
     *
     * @param appKey 角色使用场景
     * @param userId 主账号id
     * @param status 角色状态
     * @return 用户信息
     */
    public List<SubRole> findByUserIdAndStatus(String appKey, Long userId, Integer status) {
        return getSqlSession().selectList(sqlId("findByUserIdAndStatus"),
                ImmutableMap.of("appKey", appKey, "userId", userId, "status", status));
    }


    /**
     *查询角色信息
     *
     * @param appKey 角色使用场景
     * @param farmId 猪场id
     * @param status 角色状态
     * @return 用户信息
     */
    public List<SubRole> findByFarmIdAndStatus(String appKey, Long farmId, Integer status) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndStatus"),
                ImmutableMap.of("appKey", appKey, "farmId", farmId, "status", status));
    }

    public Paging<SubRole> pagingMainRole(Integer offset, Integer limit,SubRole criteria) {
        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {
            Map<String, Object> objMap = (Map) JsonMapper.nonDefaultMapper().getMapper().convertValue(criteria, Map.class);
            params.putAll(objMap);
        }

        Long total = (Long)this.sqlSession.selectOne(this.sqlId("countMainRole"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            params.put("offset", offset);
            params.put("limit", limit);
            List<SubRole> datas = this.sqlSession.selectList(this.sqlId("pagingMainRole"), params);
            return new Paging(total, datas);
        }
    }

}
