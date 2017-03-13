package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorEventRelation;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xjn on 17/3/13.
 */
@Repository
public class DoctorEventRelationDao extends MyBatisDao<DoctorEventRelation> {

    /**
     * 原事件id和目标类型查询
     * @param originEventId 原事件id
     * @param triggerTargetType 处发事件的目标类型
     * @return 关联关系
     */
    public DoctorEventRelation findByOriginAndType(Long originEventId, Integer triggerTargetType){
        return getSqlSession().selectOne(sqlId("findByOriginAndType"), ImmutableMap.of("originEventId", originEventId, "triggerTargetType", triggerTargetType));
    }

    /**
     * 原事件id查询
     * @param originEventId 原事件id
     * @return 关联关系
     */
    public List<DoctorEventRelation> findByOrigin(Long originEventId){
        return getSqlSession().selectOne(sqlId("findByOrigin"), originEventId);
    }

    /**
     * 由触发事件id获取事件关联关系
     * @param triggerEventId 触发事件id
     * @return 关联关系
     */
    public DoctorEventRelation findByTrigger(Long triggerEventId){
        return getSqlSession().selectOne(sqlId("findByTrigger"), triggerEventId);
    }

    /**
     * 根据原事件id批量更新关联关系状态
     * @param originEventIdList 原事件id
     * @param status 要设置的状态
     */
    public void updateStatusByOrigin(List<Long> originEventIdList, Integer status) {
        getSqlSession().update(sqlId("updateStatusByOrigin"), ImmutableMap.of("list", originEventIdList, "status", status));
    }
}
