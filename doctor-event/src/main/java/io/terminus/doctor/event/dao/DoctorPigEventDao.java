package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigEventDao extends MyBatisDao<DoctorPigEvent> {

    public void deleteByFarmId(Long farmId) {
        getSqlSession().delete(sqlId("deleteByFarmId"), farmId);
    }

    public DoctorPigEvent queryLastPigEventById(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastPigEventById"), pigId);
    }

    /**
     * 获取母猪流转中最新的事件
     *
     * @param pigId 猪Id
     * @param types 事件类型
     * @return
     */
    public DoctorPigEvent queryLastPigEventInWorkflow(Long pigId, List<Integer> types) {
        return this.getSqlSession().selectOne(sqlId("queryLastPigEventInWorkflow"), MapBuilder.<String, Object>of().put("pigId", pigId).put( "types", types).map());
    }


    /**
     * 查询这头母猪,该胎次下最近一次初配事件
     *
     * @param pigId
     * @return
     */
    public DoctorPigEvent queryLastFirstMate(Long pigId, Integer parity) {
        return this.getSqlSession().selectOne(sqlId("queryLastFirstMate"), ImmutableMap.of("pigId", pigId, "type", PigEvent.MATING.getKey(), "parity", parity, "currentMatingCount", 1));
    }

    /**
     * 查询这头母猪,该胎次下最近一次分娩时间
     *
     * @param pigId
     * @return
     */
    public DoctorPigEvent queryLastFarrowing(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastFarrowing"), ImmutableMap.of("pigId", pigId, "type", PigEvent.FARROWING.getKey()));
    }

    /**
     * 获取PigId 对应的 所有事件
     *
     * @param pigId
     * @return
     */
    public List<DoctorPigEvent> queryAllEventsByPigId(Long pigId) {
        return this.getSqlSession().selectList(sqlId("queryAllEventsByPigId"), pigId);
    }

    /**
     * 通过pigId 修改Event相关事件信息
     *
     * @param params 修改对应的参数
     * @return
     */
    public Boolean updatePigEventFarmIdByPigId(Map<String, Object> params) {
        return this.getSqlSession().update(sqlId("updatePigEventFarmIdByPigId"), params) >= 0;
    }

    public Long countPigEventTypeDuration(Long farmId, Integer eventType, Date startDate, Date endDate) {
        return this.getSqlSession().selectOne(sqlId("countPigEventTypeDuration"),
                ImmutableMap.of("farmId", farmId, "eventType", eventType,
                        "startDate", startDate, "endDate", endDate));
    }

    public List<Long> queryAllFarmInEvent() {
        return this.getSqlSession().selectList(sqlId("queryAllFarmInEvent"));
    }

    /**
     * 根据猪场id和Kind查询
     *
     * @param farmId 猪场id
     * @param kind   猪类(公猪, 母猪)
     * @return 事件list
     */
    public List<DoctorPigEvent> findByFarmIdAndKind(Long farmId, Integer kind) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndKind"), ImmutableMap.of("farmId", farmId, "kind", kind));
    }

    /**
     * 仅更新relEventId
     *
     * @param pigEvent relEventId
     */
    public void updateRelEventId(DoctorPigEvent pigEvent) {
        getSqlSession().update(sqlId("updateRelEventId"), pigEvent);
    }

    /**
     * 获取初次配种时间(公猪)
     */
    public DoctorPigEvent getFirstMatingTime(Map<String, Object> criteria) {
        return this.getSqlSession().selectOne(sqlId("getFirstMatingTime"), criteria);
    }
}
