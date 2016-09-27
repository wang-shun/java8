package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 猪群事件表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorGroupEventDao extends MyBatisDao<DoctorGroupEvent> {

    public List<DoctorGroupEvent> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public List<DoctorGroupEvent> findByGroupId(Long groupId) {
        return getSqlSession().selectList(sqlId("findByGroupId"), groupId);
    }

    /**
     * 根据猪群id更新猪群号
     * @param groupId 猪群id
     */
    public void updateGroupCodeByGroupId(Long groupId, String groupCode) {
        if (notEmpty(groupCode)) {
            getSqlSession().update(sqlId("updateGroupCodeByGroupId"),
                    ImmutableMap.of("groupId", groupId, "groupCode", groupCode));
        }
    }

    public List<DoctorGroupEvent> findGroupEventsByEventTypeAndDate(Long farmId, Integer eventType, Date startAt, Date endAt) {
        return getSqlSession().selectList(sqlId("findGroupEventsByEventTypeAndDate"), MapBuilder.<String, Object>of()
                .put("farmId", farmId)
                .put("eventType", eventType)
                .put("startAt", startAt)
                .put("endAt", endAt)
                .map());
    }

    /**
     * 查询一个猪舍累计有多少个事件
     * @param barnId 猪舍id
     * @return
     */
    public Long countByBarnId(Long barnId){
        return sqlSession.selectOne(sqlId("countByBarnId"), barnId);
    }

    /**
     * 查询指定时间段内发生的事件, 匹配的是 eventAt
     * @param beginDate
     * @param endDate
     * @return 返回限制数量5000条
     */
    public List<DoctorGroupEvent> findByDateRange(Date beginDate, Date endDate){
        Map<String, Object> param = new HashMap<>();
        param.put("beginDate", beginDate);
        param.put("endDate", endDate);
        return sqlSession.selectList(sqlId("findByDateRange"), ImmutableMap.copyOf(Params.filterNullOrEmpty(param)));
    }
}
