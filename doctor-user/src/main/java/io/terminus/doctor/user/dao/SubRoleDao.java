package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.SubRole;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by houly on 16/5/24.
 */
@Repository
public class SubRoleDao extends MyBatisDao<SubRole> {

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

}
