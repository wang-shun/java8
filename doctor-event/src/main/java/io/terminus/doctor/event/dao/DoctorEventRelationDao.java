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
//    public DoctorEventRelation findByOriginAndType(Long originEventId, Integer triggerTargetType){
//        return getSqlSession().selectOne(sqlId("findByOriginAndType"), ImmutableMap.of("originEventId", originEventId, "triggerTargetType", triggerTargetType));
//    }
//
//    /**
//     * 原事件id查询
//     * @param originEventId 原事件id
//     * @return 关联关系
//     */
//    public List<DoctorEventRelation> findByOrigin(Long originEventId){
//        return getSqlSession().selectList(sqlId("findByOrigin"), originEventId);
//    }
//
//    /**
//     * 由触发事件id获取事件关联关系
//     * @param triggerEventId 触发事件id
//     * @return 关联关系
//     */
//    public DoctorEventRelation findByTrigger(Long triggerEventId){
//        return getSqlSession().selectOne(sqlId("findByTrigger"), triggerEventId);
//    }

    /**
     * 更新猪事件有关正在处理中的关联关系
     * @param eventIdList 事件列表(原事件或触发事件)
     * @param status 状态
     */
    public void updatePigEventStatusUnderHandling(List<Long> eventIdList, Integer status) {
        getSqlSession().update(sqlId("updatePigEventStatusUnderHandling"), ImmutableMap.of("list", eventIdList, "status", status));
    }

    /**
     * 批量更新关联关系状态
     * @param originEventIdList 事件列表(原事件或触发事件)
     * @param status 要设置的状态
     */
    public void updateGroupEventStatus(List<Long> originEventIdList, Integer status) {
        getSqlSession().update(sqlId("updateGroupEventStatus"), ImmutableMap.of("list", originEventIdList, "status", status));
    }

    /**
     * 批量更新关联关系状态
     * @param originEventIdList 事件列表(原事件或触发事件)
     * @param status 要设置的状态
     */
    public void updatePigEventStatus(List<Long> originEventIdList, Integer status) {
        getSqlSession().update(sqlId("updatePigEventStatus"), ImmutableMap.of("list", originEventIdList, "status", status));
    }

    public DoctorEventRelation findByPigTrigger(Long pigTriggerEventId) {
        return getSqlSession().selectOne(sqlId("findByPigTrigger"), pigTriggerEventId);
    }

    public DoctorEventRelation findByGroupTrigger(Long groupTriggerEventId) {
        return getSqlSession().selectOne(sqlId("findByGroupTrigger"), groupTriggerEventId);
    }

    public List<DoctorEventRelation> findByPigOrigin(Long pigOriginEventId) {
        return getSqlSession().selectList(sqlId("findByPigOrigin"), pigOriginEventId);
    }

    public List<DoctorEventRelation> findByGroupOrigin(Long groupOriginEventId) {
        return getSqlSession().selectList(sqlId("findByGroupOrigin"), groupOriginEventId);
    }

    public DoctorEventRelation findGroupEventByGroupOrigin(Long groupOriginEventId) {
        return getSqlSession().selectOne(sqlId("findGroupEventByGroupOrigin"), groupOriginEventId);
    }

    public DoctorEventRelation findPigEventByGroupOrigin(Long groupOriginEventId) {
        return getSqlSession().selectOne(sqlId("findPigEventByGroupOrigin"), groupOriginEventId);
    }

    public DoctorEventRelation findGroupEventByPigOrigin(Long pigOriginEventId) {
        return getSqlSession().selectOne(sqlId("findGroupEventByPigOrigin"), pigOriginEventId);
    }

    public DoctorEventRelation findPigEventByPigOrigin(Long pigOriginEventId) {
        return getSqlSession().selectOne(sqlId("findPigEventByPigOrigin"), pigOriginEventId);
    }
}
