package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.PigScoreApply;
import org.springframework.stereotype.Repository;

/**
 * Desc: 猪场评分功能申请
 * Mail: hehaiyang@terminus.io
 * Date: 2017/05/02
 */
@Repository
public class PigScoreApplyDao extends MyBatisDao<PigScoreApply> {

    public PigScoreApply findByFarmIdAndUserId(Long orgId, Long farmId, Long userId){
        return sqlSession.selectOne(sqlId("findByFarmIdAndUserId"), ImmutableMap.of("orgId", orgId, "farmId", farmId, "userId", userId));
    }

}
