package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.dto.DoctorFarmEarlyEventAtDto;
import io.terminus.doctor.event.dto.DoctorPigSalesExportDto;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
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

    /**
     * 查询最新猪群事件
     */
    public DoctorGroupEvent findLastEventByGroupId(Long groupId) {
        return getSqlSession().selectOne(sqlId("findLastEventByGroupId"), groupId);
    }

    /**
     * 查询最新影响事件不包含某些事件类型的最新事件
     */
    public DoctorGroupEvent findLastEventExcludeTypes(Long groupId, List<Integer> types) {
        return getSqlSession().selectOne(sqlId("findLastEventExcludeTypes"), ImmutableMap.of("groupId", groupId, "types", types));
    }

    /**
     * 查询最新影响事件不包含某些事件类型的事件
     */
    public List<DoctorGroupEvent> findEventIncludeTypes(Long groupId, List<Integer> types) {
        return getSqlSession().selectList(sqlId("findEventIncludeTypes"), ImmutableMap.of("groupId", groupId, "types", types));
    }

    /**
     * 查询最新影响事件不包含某些事件类型的事件 逆序
     */
    public List<DoctorGroupEvent> findEventIncludeTypesForDesc(Long groupId, List<Integer> types, String eventAt) {
        return getSqlSession().selectList(sqlId("findEventIncludeTypesForDesc"),
                ImmutableMap.of("groupId", groupId, "types", types, "eventAt", eventAt));
    }

    /**
     * 查询最新手动猪群事件
     */
    public DoctorGroupEvent findLastManualEventByGroupId(Long groupId) {
        return getSqlSession().selectOne(sqlId("findLastManualEventByGroupId"), groupId);
    }

    /**
     * 根据关联猪群事件id查询（只有自动生成的事件才有关联id！）
     * @param relGroupEventId 关联事件id
     * @param type 事件类型
     * @return 关联的事件
     */
    public DoctorGroupEvent findByRelGroupEventIdAndType(Long relGroupEventId, Integer type) {
        return getSqlSession().selectOne(sqlId("findByRelGroupEventIdAndType"), ImmutableMap.of("relGroupEventId", relGroupEventId, "type", type));
    }

    /**
     * 根据关联猪事件id查询（只有自动生成的事件才有关联id！）
     * @param relPigEventId 关联事件id
     * @return 关联的事件
     */
    public DoctorGroupEvent findByRelPigEventId(Long relPigEventId) {
        return getSqlSession().selectOne(sqlId("findByRelPigEventId"), relPigEventId);
    }

    /**
     * 根据关联猪事件id查询（只有自动生成的事件才有关联id！）
     * @param relPigEventId 关联事件id
     * @param type 事件类型
     * @return 关联的事件
     */
    public DoctorGroupEvent findByRelPigEventIdAndType(Long relPigEventId, Integer type) {
        return getSqlSession().selectOne(sqlId("findByRelPigEventIdAndType"), ImmutableMap.of("relPigEventId", relPigEventId, "type", type));
    }

    /**
     * 能够回滚的事件
     * @param criteria
     * @return
     */
    public DoctorGroupEvent canRollbackEvent(Map<String, Object> criteria){
        return getSqlSession().selectOne(sqlId("canRollbackEvent"), criteria);
    }

    /**
     *根据条件查询操作人列表
     * @param criteria
     * @return
     */
    public List<DoctorEventOperator> findOperators(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("findOperators"), criteria);
    }


    /**
     * 查询猪群某一事件类型的最新事件
     * @param groupId 猪群id
     * @param type 事件类型
     * @return 最新事件
     */
    public DoctorGroupEvent findLastGroupEventByType(Long groupId, Integer type) {
        return sqlSession.selectOne(sqlId("findLastGroupEventByType"), ImmutableMap.of("groupId", groupId, "type", type));
    }


    /**
     * 获取猪群初始事件
     * @param groupId 猪群id
     * @return 猪群新建事件
     */
    public DoctorGroupEvent findInitGroupEvent(Long groupId) {
        return getSqlSession().selectOne(sqlId("findInitGroupEvent"), groupId);
    }


    /**
     * 获取猪群的所有事件,按发生事件升序排序
     * @param groupId
     * @return
     */
    public List<DoctorGroupEvent> findLinkedGroupEventsByGroupId(Long groupId){
        return getSqlSession().selectList(sqlId("findLinkedGroupEventsByGroupId"), groupId);
    }

    public Boolean updateGroupEventStatus(List<Long> ids, Integer status) {
        return Boolean.valueOf(getSqlSession().update(sqlId("updateGroupEventStatus"), MapBuilder.newHashMap().put("ids", ids, "status", status).map()) == 1);
    }

    /**
     * 查询所有事件,包括无效事件
     * @param id 事件id
     * @return 事件
     */
    public DoctorGroupEvent findEventById(Long id) {
        return getSqlSession().selectOne(sqlId("findEventById"), id);
    }

    /**
     * 更改猪场名
     * @param farmId 需要更改的猪场id
     * @param farmName 新的猪场名
     */
    public void updateFarmName(Long farmId, String farmName) {
        getSqlSession().update(sqlId("updateFarmName"), ImmutableMap.of("farmId", farmId, "farmName", farmName));
    }

    /**
     * 获取已有相应断奶事件的猪断奶事件ids
     * @param farmId 猪场id
     * @return 猪断奶事件列表
     */
    public List<Long> queryRelPigEventIdsByGroupWeanEvent(Long farmId) {
        return getSqlSession().selectList(sqlId("queryRelPigEventIdsByGroupWeanEvent"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 删除猪场后面补录的断奶事件
     * @param farmId 猪场id
     */
    public void deleteAddWeanEvents(Long farmId) {
        getSqlSession().delete(sqlId("deleteAddWeanEvents"), farmId);
    }

    public DoctorGroupEvent findNewGroupByGroupId(Long groupId) {
        return getSqlSession().selectOne(sqlId("findNewGroupByGroupId"), groupId);
    }

    public DoctorGroupEvent findCloseGroupByGroupId(Long groupId) {
        return getSqlSession().selectOne(sqlId("findCloseGroupByGroupId"), groupId);
    }

    /**
     * 获取猪群当前数量
     * @param groupId 猪群id
     * @return 猪群数量
     */
    public Integer getEventCount(Long groupId) {
        return getSqlSession().selectOne(sqlId("getEventCount"), groupId);
    }

    /**
     * 更新事件包括字段为null
     * @param groupEvent 猪群事件
     * @return 更新是否成功
     */
    public Boolean updateIncludeNull(DoctorGroupEvent groupEvent) {
        return getSqlSession().update(sqlId("updateIncludeNull"), groupEvent) == 1;
    }

    /**
     * 删除猪场下的所有猪群事件
     * @param farmId 猪场id
     */
    public void deleteByFarmId(Long farmId) {
        deleteByFarmId(farmId, Lists.newArrayList());
    }

    public void deleteByFarmId(Long farmId, List<Integer> pigTypes)  {
        getSqlSession().delete(sqlId("deleteByFarmId"), ImmutableMap.of("farmId", farmId, "pigTypes", pigTypes));
    }

    public void deleteByGroupId(Long groupId) {
        getSqlSession().delete(sqlId("deleteByGroupId"), groupId);
    }

    public List<DoctorFarmEarlyEventAtDto> getFarmEarlyEventAt(String startDate)  {
        return sqlSession.selectList(sqlId("getFarmEarlyEventAt"), startDate);
    }

    /**
     * 修复日龄,请勿使用
     */
    @Deprecated
    public List<Long> findAllGroupIdWithChangeNoAvgDayAge() {
        return sqlSession.selectList(sqlId("findAllGroupIdWithChangeNoAvgDayAge"));
    }

    /**
     * 修复日龄,请勿使用
     * @param id
     * @param avgDayAge
     */
    @Deprecated
    public void updateAvgDayAge(Long id, Integer avgDayAge) {
        sqlSession.update(sqlId("updateAvgDayAge"), ImmutableMap.of("id", id, "avgDayAge", avgDayAge));
    }

    public List<DoctorPigSalesExportDto> findNurseSales(Map<String, Object> map) {
        return sqlSession.selectList(sqlId("findNurseSales"), map);
    }
    public List<DoctorPigSalesExportDto> findCareSales(Map<String, Object> map) {
        return sqlSession.selectList(sqlId("findCareSales"), map);
    }

    public List<DoctorPigSalesExportDto> findReverseSales(Map<String, Object> map) {
        return sqlSession.selectList(sqlId("findReverseSales"), map);
    }

    public List<DoctorPigSalesExportDto> findFattenSales(Map<String, Object> map) {
        return sqlSession.selectList(sqlId("findFattenSales"), map);
    }

    public List<DoctorFarmEarlyEventAtDto> findEarLyAt(){
        return sqlSession.selectList(sqlId("findEarLyAt"));
    }

    public List<DoctorGroupEvent> findGroupEvents(Long farmId,Long relPigEventId){
        Map<String, Object> param = new HashMap<>();
        param.put("farmId", farmId);
        param.put("relPigEventId", relPigEventId);
        return sqlSession.selectList(sqlId("findGroupEvents"),param);
    }

    public List<Map<String,Object>> groupList(Long farmId,Date time,Long barn,String groupCode,String operatorName,Integer groupType,Date buildBeginGroupTime,Date buildEndGroupTime,Date closeBeginGroupTime,Date closeEndGroupTime){
        Map<String, Object> param = new HashMap<>();
        param.put("farmId", farmId);
        param.put("time", time);
        param.put("barn", barn);
        param.put("groupCode", groupCode);
        param.put("operatorName", operatorName);
        param.put("groupType", groupType);
        param.put("buildBeginGroupTime", buildBeginGroupTime);
        param.put("buildEndGroupTime", buildEndGroupTime);
        param.put("closeBeginGroupTime", closeBeginGroupTime);
        param.put("closeEndGroupTime", closeEndGroupTime);
        return sqlSession.selectList(sqlId("groupList"),param);
    }
    public List<Map<String,Object>> groupList1(Long farmId,Date time,Long barn,String groupCode,String operatorName,Integer groupType,Date buildBeginGroupTime,Date buildEndGroupTime,Date closeBeginGroupTime,Date closeEndGroupTime){
        Map<String, Object> param = new HashMap<>();
        param.put("farmId", farmId);
        param.put("time", time);
        param.put("barn", barn);
        param.put("groupCode", groupCode);
        param.put("operatorName", operatorName);
        param.put("groupType", groupType);
        param.put("buildBeginGroupTime", buildBeginGroupTime);
        param.put("buildEndGroupTime", buildEndGroupTime);
        param.put("closeBeginGroupTime", closeBeginGroupTime);
        param.put("closeEndGroupTime", closeEndGroupTime);
        return sqlSession.selectList(sqlId("groupList1"),param);
    }
    public List<Map<String,Object>> groupList2(Long farmId,Date time,Long barn,String groupCode,String operatorName,Integer groupType,Date buildBeginGroupTime,Date buildEndGroupTime){
        Map<String, Object> param = new HashMap<>();
        param.put("farmId", farmId);
        param.put("time", time);
        param.put("barn", barn);
        param.put("groupCode", groupCode);
        param.put("operatorName", operatorName);
        param.put("groupType", groupType);
        param.put("buildBeginGroupTime", buildBeginGroupTime);
        param.put("buildEndGroupTime", buildEndGroupTime);
        return sqlSession.selectList(sqlId("groupList2"),param);
    }
    public Integer getCunlan(Long groupId,Date time){
        Map<String, Object> param = new HashMap<>();
        param.put("groupId", groupId);
        param.put("time", time);
        return sqlSession.selectOne(sqlId("getCunlan"),param);
    }
    public Double getInAvgweight(Long groupId,Date time){
        Map<String, Object> param = new HashMap<>();
        param.put("groupId", groupId);
        param.put("time", time);
        return sqlSession.selectOne(sqlId("getInAvgweight"),param);
    }
    public Double getOutAvgweight(Long groupId,Date time){
        Map<String, Object> param = new HashMap<>();
        param.put("groupId", groupId);
        param.put("time", time);
        return sqlSession.selectOne(sqlId("getOutAvgweight"),param);
    }
    public Double getAvgDayAge(Long groupId,Date time){
        Map<String, Object> param = new HashMap<>();
        param.put("groupId", groupId);
        param.put("time", time);
        return sqlSession.selectOne(sqlId("getAvgDayAge"),param);
    }
    public Date getBuildEventAt(Long groupId){
        Map<String, Object> param = new HashMap<>();
        param.put("groupId", groupId);
        return sqlSession.selectOne(sqlId("getBuildEventAt"),param);
    }
    public List<DoctorGroupEvent> findEventIncludeTypes1(Long groupId, List<Integer> types,Date time) {
        return getSqlSession().selectList(sqlId("findEventIncludeTypes1"), ImmutableMap.of("groupId", groupId, "types", types,"time",time));
    }
    public int isCloseGroup(Long groupId) {
        return getSqlSession().selectOne(sqlId("isCloseGroup"), ImmutableMap.of("groupId", groupId));
    }
    /**
     * 查询最新猪群事件
     */
    public DoctorGroupEvent lastEventByGroupId(Long groupId) {
        return getSqlSession().selectOne(sqlId("lastEventByGroupId"), groupId);
    }
}
