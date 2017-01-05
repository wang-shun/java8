package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
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

    public DoctorPigEvent findByRelGroupEventId(Long relGroupEventId) {
        return getSqlSession().selectOne(sqlId("findByRelGroupEventId"), relGroupEventId);
    }

    public DoctorPigEvent findByRelPigEventId(Long relPigEventId) {
        return getSqlSession().selectOne(sqlId("findByRelPigEventId"), relPigEventId);
    }

    public void deleteByFarmId(Long farmId) {
        getSqlSession().delete(sqlId("deleteByFarmId"), farmId);
    }

    public DoctorPigEvent queryLastPigEventById(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastPigEventById"), pigId);
    }

    /**
     *查询最新的手动事件
     * @param pigId 猪id
     * @return 最新事件
     */
    public DoctorPigEvent queryLastManualPigEventById(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastManualPigEventById"), pigId);
    }

    public DoctorPigEvent queryLastPigEventByPigIds(List<Long> pigIds) {
        return this.getSqlSession().selectOne(sqlId("queryLastPigEventByPigIds"), pigIds);
    }

    /**
     * 获取母猪流转中最新的事件
     *
     * @param pigId 猪Id
     * @param types 事件类型
     * @return
     */
    public DoctorPigEvent queryLastPigEventInWorkflow(Long pigId, List<Integer> types) {
        return this.getSqlSession().selectOne(sqlId("queryLastPigEventInWorkflow"), MapBuilder.<String, Object>of().put("pigId", pigId).put("types", types).map());
    }


    /**
     * 查询这头母猪,该胎次最近一次初配事件
     *
     * @param pigId
     * @return
     */
    public DoctorPigEvent queryLastFirstMate(Long pigId, Integer parity) {
        return this.getSqlSession().selectOne(sqlId("queryLastFirstMate"), MapBuilder.<String, Object>of().put("pigId", pigId).put("type", PigEvent.MATING.getKey()).put("parity", parity).put("currentMatingCount", 1).map());
    }

    /**
     * 查询这头母猪,该胎次最近一次分娩事件
     *
     * @param pigId
     * @return
     */
    public DoctorPigEvent queryLastFarrowing(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastEvent"), MapBuilder.<String, Object>of().put("pigId", pigId).put("type", PigEvent.FARROWING.getKey()).map());
    }

    /**
     * 查询这头母猪,该胎次最近一次妊娠检查事件
     *
     * @param pigId
     * @return
     */
    public DoctorPigEvent queryLastPregCheck(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastEvent"), MapBuilder.<String, Object>of().put("pigId", pigId).put("type", PigEvent.PREG_CHECK.getKey()).map());
    }

    /**
     * 查询这头母猪,最近一次断奶事件
     *
     * @param pigId
     * @deprecated 建议查询最近一次导致其断奶的事件，而不是单纯查询断奶事件
     * @return
     */
    @Deprecated
    public DoctorPigEvent queryLastWean(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastEvent"), MapBuilder.<String, Object>of().put("pigId", pigId).put("type", PigEvent.WEAN.getKey()).map());
    }

    /**
     * 查询这头母猪最近一次进场事件
     *
     * @param pigId
     * @return
     */
    public DoctorPigEvent queryLastEnter(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastEvent"), MapBuilder.<String, Object>of().put("pigId", pigId).put("type", PigEvent.ENTRY.getKey()).map());
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
     * 根据猪场id和Kind查询
     *
     * @param farmId 猪场id
     * @param kind   猪类(公猪, 母猪)
     * @param eventTypes 事件类型
     * @return 事件list
     */
    public List<DoctorPigEvent> findByFarmIdAndKindAndEventTypes(Long farmId, Integer kind, List<Integer> eventTypes) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndKindAndEventTypes"), MapBuilder.of()
                .put("farmId", farmId)
                .put("kind", kind)
                .put("eventTypes", eventTypes).map()
        );
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

    /**
     * 查找一只猪(在指定时间之后)的第一个事件
     * @param pigId 猪id, 不可为空
     * @param fromDate 可为空
     * @return
     */
    public DoctorPigEvent findFirstPigEvent(Long pigId, Date fromDate){
        Map<String, Object> param;
        if(fromDate == null){
            param = ImmutableMap.of("pigId", pigId);
        }else{
            param = ImmutableMap.of("pigId", pigId, "fromDate", fromDate);
        }
        return sqlSession.selectOne(sqlId("findFirstPigEvent"), param);
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
    public List<DoctorPigEvent> findByDateRange(Date beginDate, Date endDate){
        Map<String, Object> param = new HashMap<>();
        param.put("beginDate", beginDate);
        param.put("endDate", endDate);
        return sqlSession.selectList(sqlId("findByDateRange"), ImmutableMap.copyOf(Params.filterNullOrEmpty(param)));
    }

    public Boolean updates(List<DoctorPigEvent> lists){
        return  Boolean.valueOf(sqlSession.update(sqlId("updates"), lists) == 1);
    }

    /**
     *根据条件查询操作人列表
     * @param criteria
     * @return
     */
    public List<DoctorPigEvent> findOperators(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("findOperators"), criteria);
    }

    public List<DoctorPigEvent> findByPigId(Long pigId) {
        return sqlSession.selectList(sqlId("findByPigId"), pigId);
    }

    /**
     * 事件列表(修复断奶事件暂时)
     * @return
     */
    public List<DoctorPigEvent> addWeanEventAfterFosAndPigLets(){
        return sqlSession.selectList(sqlId("addWeanEventAfterFosAndPigLets"));
    }

    /**
     * 能够回滚的事件
     * @param criteria
     * @return
     */
    public DoctorPigEvent canRollbackEvent(Map<String, Object> criteria){
        return getSqlSession().selectOne(sqlId("canRollbackEvent"), criteria);
    }

    /**
     * 根据事件类型和时间区间查询
     */
    public List<DoctorPigEvent> findByFarmIdAndTypeAndDate(long farmId, int type, Date startAt, Date endAt) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndTypeAndDate"),
                ImmutableMap.of("farmId", farmId, "type", type, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 临时使用
     * @param doctorPigEvent
     */
    @Deprecated
    public void updatePigEvents(DoctorPigEvent doctorPigEvent){
        sqlSession.update("updatePigEvents", doctorPigEvent);
    }

    public void updatePigCode(Long pigId, String code) {
        sqlSession.update(sqlId("updatePigCode"), ImmutableMap.of("pigId", pigId, "pigCode", code));
    }

    /**
     * 查询母猪胎次中数据平均值
     * @param pigId
     * @return
     */
    public Map<String, Object> querySowParityAvg(Long pigId) {
        return sqlSession.selectOne("querySowParityAvg", pigId);
    }

    /**
     * 查询最新的几个事件
     * @param pigId 猪id
     * @param limit 查询几条
     * @return 猪事件列表
     */
    public List<DoctorPigEvent> limitPigEventOrderByEventAt(Long pigId, Integer limit) {
        return getSqlSession().selectList(sqlId("limitPigEventOrderByEventAt"), ImmutableMap.of("pigId", pigId, "limit", MoreObjects.firstNonNull(limit, 1)));
    }

    /**
     * 查询和猪群相关联的猪事件
     * @param groupId 猪群id
     * @return 猪事件
     */
    public List<DoctorPigEvent> findByGroupId(Long groupId) {
        return getSqlSession().selectList(sqlId("findByGroupId"), ImmutableMap.of("groupId", groupId));
    }
}
