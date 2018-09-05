package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.dto.DoctorFarmEarlyEventAtDto;
import io.terminus.doctor.event.dto.DoctorNpdExportDto;
import io.terminus.doctor.event.dto.DoctorPigSalesExportDto;
import io.terminus.doctor.event.dto.DoctorProfitExportDto;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigEventDao extends MyBatisDao<DoctorPigEvent> {

    public List<DoctorPigEvent> getabosum(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("getabosum"),criteria);
    }

    public List<DoctorPigEvent> getweansum(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("getweansum"),criteria);
    }

    public List<DoctorPigEvent> getfosterssum(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("getfosterssum"),criteria);
    }

    public List<DoctorPigEvent> getpigletssum(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("getpigletssum"),criteria);
    }

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
     * 根据查询这一事件之前的最新的一个type事件
     */
    public DoctorPigEvent findLastByTypeAndDate(Long pigId, Date eventAt, Integer type) {
        return getSqlSession().selectOne(sqlId("findLastByTypeAndDate"), ImmutableMap.of("pigId", pigId, "eventAt", eventAt, "type", type));
    }


    /**
     * 查询最新的手动事件
     *
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
     * @return
     * @deprecated 建议查询最近一次导致其断奶的事件，而不是单纯查询断奶事件
     */
    public DoctorPigEvent queryLastWean(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastEvent"), MapBuilder.<String, Object>of().put("pigId", pigId).put("type", PigEvent.WEAN.getKey()).map());
    }

    /**
     * 获取猪某一事件类型的最新事件
     *
     * @param pigId
     * @param type
     * @return
     */
    public DoctorPigEvent queryLastEventByType(Long pigId, Integer type) {
        return this.getSqlSession().selectOne(sqlId("queryLastEvent"), MapBuilder.<String, Object>of().put("pigId", pigId).put("type", type).map());
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
     * 获取PigId 对应的 所有事件 正序排列
     *
     * @param pigId 猪id
     * @return 正序列表
     */
    public List<DoctorPigEvent> queryAllEventsByPigIdForASC(Long pigId) {
        return this.getSqlSession().selectList(sqlId("queryAllEventsByPigIdForASC"), pigId);
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
     * @param farmId     猪场id
     * @param kind       猪类(公猪, 母猪)
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
     * 查找一只猪(在指定时间之后)的第一个事件
     *
     * @param pigId    猪id, 不可为空
     * @param fromDate 可为空
     * @return
     */
    public DoctorPigEvent findFirstPigEvent(Long pigId, Date fromDate) {
        Map<String, Object> param;
        if (fromDate == null) {
            param = ImmutableMap.of("pigId", pigId);
        } else {
            param = ImmutableMap.of("pigId", pigId, "fromDate", fromDate);
        }
        return sqlSession.selectOne(sqlId("findFirstPigEvent"), param);
    }

    /**
     * 查询一个猪舍累计有多少个事件
     *
     * @param barnId 猪舍id
     * @return
     */
    public Long countByBarnId(Long barnId) {
        return sqlSession.selectOne(sqlId("countByBarnId"), barnId);
    }

    /**
     * 查询指定时间段内发生的事件, 匹配的是 eventAt
     *
     * @param beginDate
     * @param endDate
     * @return 返回限制数量5000条
     */
    public List<DoctorPigEvent> findByDateRange(Date beginDate, Date endDate) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginDate", beginDate);
        param.put("endDate", endDate);
        return sqlSession.selectList(sqlId("findByDateRange"), ImmutableMap.copyOf(Params.filterNullOrEmpty(param)));
    }

    public Boolean updates(List<DoctorPigEvent> lists) {
        return Boolean.valueOf(sqlSession.update(sqlId("updates"), lists) == 1);
    }

    /**
     * 根据条件查询操作人列表
     *
     * @param criteria
     * @return
     */
    public List<DoctorEventOperator> findOperators(Map<String, Object> criteria) {
        return getSqlSession().selectList(sqlId("findOperators"), criteria);
    }

    public List<DoctorPigEvent> findByPigId(Long pigId) {
        return sqlSession.selectList(sqlId("findByPigId"), pigId);
    }


    /**
     * 事件列表(修复断奶事件暂时)
     *
     * @return
     */
    public List<DoctorPigEvent> addWeanEventAfterFosAndPigLets() {
        return sqlSession.selectList(sqlId("addWeanEventAfterFosAndPigLets"));
    }

    /**
     * 能够回滚的事件
     *
     * @param criteria
     * @return
     */
    public DoctorPigEvent canRollbackEvent(Map<String, Object> criteria) {
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
     *
     * @param doctorPigEvent
     */
    @Deprecated
    public void updatePigEvents(DoctorPigEvent doctorPigEvent) {
        sqlSession.update("updatePigEvents", doctorPigEvent);
    }

    public void updatePigCode(Long pigId, String code) {
        sqlSession.update(sqlId("updatePigCode"), ImmutableMap.of("pigId", pigId, "pigCode", code));
    }

    /**
     * 查询母猪胎次中数据平均值
     *
     * @param pigId
     * @return
     */
    public Map<String, Object> querySowParityAvg(Long pigId) {
        return sqlSession.selectOne("querySowParityAvg", pigId);
    }

    /**
     * 查询最新的几个事件
     *
     * @param pigId 猪id
     * @param limit 查询几条
     * @return 猪事件列表
     */
    public List<DoctorPigEvent> limitPigEventOrderByEventAt(Long pigId, Integer limit) {
        return getSqlSession().selectList(sqlId("limitPigEventOrderByEventAt"), ImmutableMap.of("pigId", pigId, "limit", MoreObjects.firstNonNull(limit, 1)));
    }

    /**
     * 查询和猪群相关联的猪事件
     *
     * @param groupId 猪群id
     * @return 猪事件
     */
    public List<DoctorPigEvent> findByGroupId(Long groupId) {
        return getSqlSession().selectList(sqlId("findByGroupId"), ImmutableMap.of("groupId", groupId));
    }

    /**
     * 去获取猪某一事件之后的事件列表
     *
     * @param pigId   猪id
     * @param eventId 事件id
     * @return 事件列表
     */
    public List<DoctorPigEvent> findFollowEvents(Long pigId, Long eventId) {
        return getSqlSession().selectList(sqlId("findFollowEvents"), ImmutableMap.of("eventId", eventId, "pigId", pigId));
    }

    /**
     * 批量更改事件状态
     *
     * @param ids    事件id列表
     * @param status 需要更改的状态
     */
    public void updateEventsStatus(List<Long> ids, Integer status) {
        getSqlSession().update(sqlId("updateEventsStatus"), ImmutableMap.of("ids", ids, "status", status));
    }

    /**
     * 查询所有事件,包括无效事件
     *
     * @param id 事件id
     * @return 事件
     */
    public DoctorPigEvent findEventById(Long id) {
        return getSqlSession().selectOne(sqlId("findEventById"), id);
    }

    /**
     * 查询最新影响事件不包含某些事件类型的最新事件
     */
    public DoctorPigEvent findLastEventExcludeTypes(Long pigId, List<Integer> types) {
        return getSqlSession().selectOne(sqlId("findLastEventExcludeTypes"), ImmutableMap.of("pigId", pigId, "types", types));
    }

    /*
     * 根据条件搜索猪id
     * @param criteria
     * @return
     */
    public List<Long> findPigIdsBy(Map<String, Object> criteria) {
        return getSqlSession().selectList(sqlId("findPigIdsBy"), criteria);
    }

    /**
     * 更改猪场名
     *
     * @param farmId   需要更改的猪场id
     * @param farmName 新的猪场名
     */
    public void updateFarmName(Long farmId, String farmName) {
        getSqlSession().update(sqlId("updateFarmName"), ImmutableMap.of("farmId", farmId, "farmName", farmName));
    }

    /**
     * 查询没有相对应猪群断奶事件的猪断奶事件
     *
     * @param excludeIds 过滤的事件ids
     * @return 猪断奶事件
     */
    public List<DoctorPigEvent> queryWeansWithoutGroupWean(List<Long> excludeIds, Long farmId, Integer offset, Integer limit) {
        return getSqlSession().selectList(sqlId("queryWeansWithoutGroupWean"), ImmutableMap.of("excludeIds", excludeIds, "farmId", farmId, "offset", offset, "limit", limit));
    }

    /**
     * 查询之前手动添加的断奶事件
     *
     * @return 断奶事件列表
     */
    public List<DoctorPigEvent> queryOldAddWeanEvent() {
        return getSqlSession().selectList(sqlId("queryOldAddWeanEvent"));
    }

    /**
     * 查询之前拼窝、仔猪变动触发的断奶事件
     *
     * @return 断奶事件列表
     */
    public List<DoctorPigEvent> queryTriggerWeanEvent() {
        return getSqlSession().selectList(sqlId("queryTriggerWeanEvent"));
    }

    /**
     * 计算npd的数量用于计算分页数据
     *
     * @param maps
     * @return
     */
    public Long countNpdWeanEvent(Map<String, Object> maps) {
        return getSqlSession().selectOne(sqlId("countNpdWeanEvent"), maps);
    }

    /**
     * npd的计算
     *
     * @param maps
     * @param offset
     * @param limit
     * @return
     */
    public Paging<DoctorNpdExportDto> sumNpdWeanEvent(Map<String, Object> maps, Integer offset, Integer limit) {
        maps.put("offset", offset);
        maps.put("limit", limit);

        maps = ImmutableMap.copyOf(Params.filterNullOrEmpty(maps));
        long total = countNpdWeanEvent(maps);
        if (total <= 0) {
            return new Paging<>(0L, Collections.<DoctorNpdExportDto>emptyList());
        }
        List<DoctorNpdExportDto> doctorNpdExportDtos = getSqlSession().selectList(sqlId("sumPngWeanEvent"), maps);
        return new Paging<>(total, doctorNpdExportDtos);
    }


    public Long countSaleEvent(Map<String, Object> maps) {
        return getSqlSession().selectOne(sqlId("countSales"), maps);
    }

    /**
     * 销售情况
     *
     * @param maps
     * @param offset
     * @param limit
     * @return
     */
    public Paging<DoctorPigSalesExportDto> findSalesEvent(Map<String, Object> maps, Integer offset, Integer limit) {
        maps.put("offset", offset);
        maps.put("limit", limit);
        long total = countSaleEvent(maps);
        if (total <= 0) {
            return new Paging<>(0L, Collections.<DoctorPigSalesExportDto>emptyList());
        }
        List<DoctorPigSalesExportDto> doctorPigSalesExportDtos = getSqlSession().selectList(sqlId("findSalesEvent"), maps);
        return new Paging<>(total, doctorPigSalesExportDtos);
    }

    /**
     * 获取猪某一胎次下的分娩事件
     *
     * @param pigId  猪id
     * @param parity 胎次
     * @return 分娩事件
     */
    public DoctorPigEvent getFarrowEventByParity(Long pigId, Integer parity) {
        //之前的一个bug导致会出现同一个猪有相同胎次的分娩事件
        //在计算胎次部分已做了调整，但之前还留有一部分的历史数据是错误的
        //先让这部分错误的数据查询出来，后续计算胎次时会更新成正确的胎次
        List<DoctorPigEvent> events = getSqlSession().selectList(sqlId("getFarrowEventByParity"), ImmutableMap.of("pigId", pigId, "parity", parity));

        return events.stream()
                .sorted((p1, p2) -> {
                    int result = p2.getEventAt().compareTo(p1.getEventAt());
                    if (result == 0)
                        return p2.getId().compareTo(p1.getId());
                    else return result;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取猪某一胎次下的断奶事件
     *
     * @param pigId  猪id
     * @param parity 胎次
     * @return 断奶事件
     */
    public DoctorPigEvent getWeanEventByParity(Long pigId, Integer parity) {
        return getSqlSession().selectOne(sqlId("getWeanEventByParity"), ImmutableMap.of("pigId", pigId, "parity", parity));
    }

    /**
     * 获取某时间前的影响状态的最近的事件
     *
     * @param pigId   猪id
     * @param eventAt 时间
     * @return 事件
     */
    public DoctorPigEvent getLastStatusEventBeforeEventAt(Long pigId, Date eventAt) {
        return getSqlSession().selectOne(sqlId("getLastStatusEventBeforeEventAt"), ImmutableMap.of("pigId", pigId, "eventAt", eventAt));
    }

    /**
     * 获取某时间前的包括制定id的影响状态的最近的事件
     *
     * @param pigId   猪id
     * @param eventAt 时间
     * @param id      不包括的事件id
     * @return 事件
     */
    public DoctorPigEvent getLastStatusEventBeforeEventAtExcludeId(Long pigId, Date eventAt, Long id) {
        return getSqlSession().selectOne(sqlId("getLastStatusEventBeforeEventAtExcludeId"), ImmutableMap.of("pigId", pigId, "eventAt", eventAt, "id", id));
    }

    /**
     * 获取某时间前的包括制定id的影响状态的最近的事件
     *
     * @param pigId   猪id
     * @param eventAt 时间
     * @param id      不包括的事件id
     * @return 事件
     */
    public DoctorPigEvent getLastStatusEventAfterEventAtExcludeId(Long pigId, Date eventAt, Long id) {
        return getSqlSession().selectOne(sqlId("getLastStatusEventAfterEventAtExcludeId"), ImmutableMap.of("pigId", pigId, "eventAt", eventAt, "id", id));
    }

    /**
     * 获取时间前的初配事件
     *
     * @param pigId   猪id
     * @param eventAt 时间
     * @return 初配事件
     */
    public DoctorPigEvent getFirstMateEvent(Long pigId, Date eventAt) {
        return getSqlSession().selectOne(sqlId("getFirstMateEvent"), ImmutableMap.of("pigId", pigId, "eventAt", eventAt));
    }

    /**
     * 猪的不同种类进行金额的统计
     * 利润情况
     *
     * @param maps
     * @return
     */
    public List<DoctorProfitExportDto> sumProfitPigType(Map<String, Object> maps) {
        maps = Params.filterNullOrEmpty(maps);
        return getSqlSession().selectList(sqlId("sumProFitPigType"), maps);
    }

    /**
     * 删除猪转场触发的事件
     */
    public void deleteByChgFarm(Long pigId) {
        getSqlSession().delete(sqlId("deleteByChgFarm"), pigId);
    }

    /**
     * 获取离场前的最新事件
     *
     * @param pigId 猪id
     * @param id    离场事件id
     * @return 离场前的最新事件
     */
    public DoctorPigEvent getLastEventBeforeRemove(Long pigId, Long id) {
        return getSqlSession().selectOne(sqlId("getLastEventBeforeRemove"), ImmutableMap.of("pigId", pigId, "id", id));
    }

    /**
     * 更新事件包括字段为null
     *
     * @param pigEvent 猪事件
     * @return 更新是否成功
     */
    public Boolean updateIncludeNull(DoctorPigEvent pigEvent) {
        return getSqlSession().update(sqlId("updateIncludeNull"), pigEvent) == 1;
    }

    /**
     * 获取最新的胎次
     *
     * @param pigId 猪id
     * @return 最新胎次
     */
    public Integer findLastParity(Long pigId) {
        return getSqlSession().selectOne(sqlId("findLastParity"), pigId);
    }

    /**
     * 获取某一头某一胎次下未断奶数量
     *
     * @param pigId  猪id
     * @param parity 胎次
     * @return 未断奶数
     */
    public Integer findUnWeanCountByParity(Long pigId, Integer parity) {
        return getSqlSession().selectOne(sqlId("findUnWeanCountByParity"), ImmutableMap.of("pigId", pigId, "parity", parity));
    }

    /**
     * 获取最新的去除(事件类型:3,4,5,8)
     *
     * @param pigId 猪id
     * @return 事件
     * @see PigEvent
     */
    public DoctorPigEvent getLastStatusEvent(Long pigId) {
        return getSqlSession().selectOne(sqlId("getLastStatusEvent"), pigId);
    }

    /**
     * 获取某猪某胎次下,妊娠检查时间前最近的初配事件
     *
     * @param pigId  猪id
     * @param parity 胎次
     * @param id     妊娠检查事件id
     * @return 初配事件
     */
    public DoctorPigEvent getFirstMatingBeforePregCheck(Long pigId, Integer parity, Long id) {
        return getSqlSession().selectOne(sqlId("getFirstMatingBeforePregCheck"),
                ImmutableMap.of("pigId", pigId, "parity", parity, "id", id));
    }

    public List<DoctorPigEvent> findEffectMatingCountByPigIdForAsc(Long pigId) {
        return sqlSession.selectList(sqlId("findEffectMatingCountByPigIdForAsc"), pigId);
    }


    /**
     * 将猪场中转场转入事件前的事件置为eventSource=5
     *
     * @param list 猪场id列表
     */
    public void flushChgFarmEventSource(List<Long> list) {
        sqlSession.update(sqlId("flushChgFarmEventSource"), list);
    }

    /**
     * 获取猪场的事件
     *
     * @param list 猪场id列表
     * @return 事件列表
     */
    public List<DoctorPigEvent> findByFarmIds(List<Long> list) {
        return sqlSession.selectList(sqlId("findByFarmIds"), list);
    }

    /**
     * 获取当前母猪未断奶数量
     *
     * @param pigId 母猪id
     * @return 未断奶数量
     */
    public Integer getSowUnweanCount(Long pigId) {
        return sqlSession.selectOne(sqlId("getSowUnweanCount"), pigId);
    }

    public List<DoctorFarmEarlyEventAtDto> getFarmEarlyEventAt(String startDate) {
        return sqlSession.selectList(sqlId("getFarmEarlyEventAt"), startDate);
    }

    /**
     * 查询导致猪到达当前的状态事件的日期
     *
     * @param pigId  猪id
     * @param status 猪状态
     * @return 事件日期
     */
    public Date findEventAtLeadToStatus(Long pigId, Integer status) {
        return getSqlSession().selectOne(sqlId("findEventAtLeadToStatus"),
                ImmutableMap.of("pigId", pigId, "status", status));
    }

    /**
     *
     */
    public Date findMateEventToPigId(Long pigId) {
        return getSqlSession().selectOne(sqlId("findMateEventToPigId"),pigId);
    }

    /**
     * 修复窝号临时创建请勿使用
     *
     * @return
     */
    @Deprecated
    public List<DoctorPigEvent> findAllFarrowNoNestCode() {
        return sqlSession.selectList(sqlId("findAllFarrowNoNestCode"));
    }

    /**
     * 修复窝号,临时创建请勿使用
     *
     * @return
     */
    @Deprecated
    public void insertNestCode(Long farmId, String begin, String end) {
        sqlSession.update(sqlId("insertNestCode"), ImmutableMap.of("farmId", farmId
                , "begin", begin, "end", end));
    }

    public List<DoctorPigSalesExportDto> findSales(Map<String, Object> map) {
        return sqlSession.selectList(sqlId("findSales"), map);
    }


    /**
     * 获取需要参与计算NPD的事件
     * 过滤公猪事件
     * 过滤母猪事件中，防疫，疾病，体况，拼窝，被拼窝，转舍，仔猪变动类型的事件
     * 只查询farmId，pigId，type，preCheckResult，eventAt字段
     *
     * @param farmId
     * @param start
     * @param end
     * @return
     */
    public List<DoctorPigEvent> findForNPD(Long farmId, Date start, Date end) {

        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", start);
        params.put("endDate", end);
        params.put("farmId", farmId);
        return sqlSession.selectList(sqlId("findForNPD"), params);
    }

    /**
     * 查询在指定时间段内有事件发生的猪
     *
     * @return
     */
    public List<Long> findPigAtEvent(Date start, Date end, List<Long> farmIds) {

        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", start);
        params.put("endDate", end);
        params.put("farmIds", farmIds);
        params.put("kind", 1);

        return this.sqlSession.selectList(this.sqlId("findPigAt"), params);
    }

    public List<DoctorFarmEarlyEventAtDto> findEarLyAt() {
        return sqlSession.selectList(sqlId("findEarLyAt"));
    }

    /**
     * 计算某次配种的胎次
     *
     * @param pigId
     * @param matingEventAt
     * @return
     */
    public Integer countParity(Long pigId, Date matingEventAt) {

        int basicParity = 0;

        List<DoctorPigEvent> entryEvents = findByPigAndType(pigId, PigEvent.ENTRY);
        if (!entryEvents.isEmpty() && null != entryEvents.get(0).getParity()) {
            basicParity = entryEvents.get(0).getParity();
        }

        return basicParity + countEvent(pigId, matingEventAt, PigEvent.WEAN);
    }

    /**
     * 计算某一个时间点之前的某中类型事件发生的次数
     *
     * @param pigId
     * @param eventAt
     * @return
     */
    public Integer countEvent(Long pigId, Date eventAt, PigEvent eventType) {

        if (null == pigId || null == eventAt || null == eventAt)
            return 0;

        Map<String, Object> params = new HashMap<>();
        params.put("beforeAt", eventAt);
        params.put("pigId", pigId);
        params.put("type", eventType.getKey());

        return this.sqlSession.selectOne(this.sqlId("countEvent"), params);
    }


    public List<DoctorPigEvent> findByPigAndType(Long pigId, PigEvent eventType) {

        Map<String, Object> params = new HashMap<>();
        params.put("pigId", pigId);
        params.put("type", eventType.getKey());

        return this.sqlSession.selectList(this.sqlId("findByPigAndType"), params);
    }

    /**
     * 断奶到配种事件的数量
     * @param pigId 猪id
     * @return 事件数量
     */
    public Integer findWeanToMatingCount(Long pigId) {
        return getSqlSession().selectOne(sqlId("findWeanToMatingCount"), pigId);
    }

    public Boolean flushParityAndBeforeStatusAndAfterStatus(List<DoctorPigEvent> list){
        return getSqlSession().update(sqlId("flushParityAndBeforeStatusAndAfterStatus"), list) == 1;
    }

    /**
     * 查询错误数据（修复数据临时使用）
     * @param farmId
     * @return
     */
    @Deprecated
    public List<DoctorPigEvent> queryToMatingForTime(Long farmId) {
        return getSqlSession().selectList(sqlId("queryToMatingForTime"), farmId);
    }

    public List<DoctorPigEvent> queryEventsForDescBy(Long pigId, Integer parity) {
        return getSqlSession().selectList(sqlId("queryEventsForDescBy"),
                ImmutableMap.of("pigId", pigId, "parity", parity));
    }


    /**
     * 查询转场之前的事件
     * @param pigId
     * @param eventId
     * @return
     */
    public List<DoctorPigEvent> queryBeforeChgFarm(Long pigId, Long eventId){
        return getSqlSession().selectList(sqlId("queryBeforeChgFarm"),
                ImmutableMap.of("pigId", pigId, "eventId", eventId));
    }

    /**
     * 根据当前时间查上一关键事件
     * 分娩：查上一次配种，转入
     * 断奶：查上一次分娩，转入
     * 离场：查上一次相关事件
     * 配种，阴性:查上一次进场，转入，断奶
     * @see io.terminus.doctor.event.enums.PigEvent
     * @param event
     * @return
     */
    public DoctorPigEvent queryBeforeEvent(DoctorPigEvent event){
        Map<String, Object> params = new HashMap<>();
        params.put("id",event.getId());
        params.put("eventAt", event.getEventAt());
        params.put("pigId", event.getPigId());
        params.put("type",event.getType());
        return this.sqlSession.selectOne(this.sqlId("queryBeforeEvent"), params);
    }

    public Long queryEventId(Long pigId){
        return getSqlSession().selectOne(sqlId("queryEventId"), pigId);
    }



    /**
     * 事件筛选母猪的ID
     * @param criteria
     * @return
     */
    public List<Long> findPigIdsByEvent(Map<String, Object> criteria) {
        return getSqlSession().selectList(sqlId("findPigIdsByEvent"), criteria);
    }

    public Date findFarmSowEventAt(Long pigId, Long farmId) {
        return getSqlSession().selectOne(sqlId("findFarmSowEventAt"),
                ImmutableMap.of("pigId", pigId, "farmId", farmId));
    }

    public List<Map<String,Object>> getInFarmPigId(Long farmId, Date time,String pigCode,Integer breed,Date beginInFarmTime, Date endInFarmTime,Integer parity,Integer pigStatus,String operatorName,Long barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("time",time);
        map.put("pigCode",pigCode);
        map.put("breed",breed);
        map.put("beginInFarmTime",beginInFarmTime);
        map.put("endInFarmTime",endInFarmTime);
        map.put("parity",parity);
        map.put("pigStatus",pigStatus);
        map.put("operatorName",operatorName);
        map.put("barnId",barnId);
        return this.sqlSession.selectList(this.sqlId("getInFarmPigId"), map);
    }
    public List<Map<String,Object>> getInFarmPigId1(Long farmId, Date time,String pigCode,Integer breed,Date beginInFarmTime, Date endInFarmTime,Integer parity,Integer pigStatus,String operatorName,Long barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("time",time);
        map.put("pigCode",pigCode);
        map.put("breed",breed);
        map.put("beginInFarmTime",beginInFarmTime);
        map.put("endInFarmTime",endInFarmTime);
        map.put("parity",parity);
        map.put("pigStatus",pigStatus);
        map.put("operatorName",operatorName);
        map.put("barnId",barnId);
        return this.sqlSession.selectList(this.sqlId("getInFarmPigId"), map);
    }
    public List<Map<String,Object>> getInFarmPigId2(Long farmId, Date time,String pigCode,Integer breed,Date beginInFarmTime, Date endInFarmTime){
        Map<String, Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("time",time);
        map.put("pigCode",pigCode);
        map.put("breed",breed);
        map.put("beginInFarmTime",beginInFarmTime);
        map.put("endInFarmTime",endInFarmTime);
        return this.sqlSession.selectList(this.sqlId("getInFarmPigId2"), map);
    }
    public List<Map<String,Object>> getInFarmPigId3(Long farmId, Date time,String pigCode,Integer breed,Date beginInFarmTime, Date endInFarmTime){
        Map<String, Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("time",time);
        map.put("pigCode",pigCode);
        map.put("breed",breed);
        map.put("beginInFarmTime",beginInFarmTime);
        map.put("endInFarmTime",endInFarmTime);
        return this.sqlSession.selectList(this.sqlId("getInFarmPigId3"), map);
    }
    public List<Map<String,Object>> getInFarmBoarId(Long farmId,Date queryDate,String pigCode,Integer breedId,Integer pigStatus, Date beginDate,Date endDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("queryDate",queryDate);
        map.put("pigCode",pigCode);
        map.put("breedId",breedId);
        map.put("pigStatus",pigStatus);
        map.put("beginDate",beginDate);
        map.put("endDate",endDate);
        return this.sqlSession.selectList(this.sqlId("getInFarmBoarId"), map);
    }
    public List<Map<String,Object>> getInFarmBoarId1(Long farmId,Integer pigType,Date queryDate,Integer barnId,String pigCode,Integer breedId,String staffName, Date beginDate,Date endDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("pigType",pigType);
        map.put("queryDate",queryDate);
        map.put("barnId",barnId);
        map.put("pigCode",pigCode);
        map.put("breedId",breedId);
        map.put("staffName",staffName);
        map.put("beginDate",beginDate);
        map.put("endDate",endDate);
        return this.sqlSession.selectList(this.sqlId("getInFarmBoarId1"), map);
    }
    public List<Map<String,Object>> getInFarmBoarId2(Long farmId,Integer pigType,Date queryDate,Integer barnId,String pigCode,Integer breedId,String staffName, Date beginDate,Date endDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("pigType",pigType);
        map.put("queryDate",queryDate);
        map.put("barnId",barnId);
        map.put("pigCode",pigCode);
        map.put("breedId",breedId);
        map.put("staffName",staffName);
        map.put("beginDate",beginDate);
        map.put("endDate",endDate);
        return this.sqlSession.selectList(this.sqlId("getInFarmBoarId2"), map);
    }
    public List<Map<String,Object>> getInFarmBoarId3(Long farmId,Date queryDate,Integer barnId,String pigCode,Integer breedId,String staffName, Date beginDate,Date endDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("farmId", farmId);
        map.put("queryDate", queryDate);
        map.put("barnId", barnId);
        map.put("pigCode", pigCode);
        map.put("breedId", breedId);
        map.put("staffName", staffName);
        map.put("beginDate", beginDate);
        map.put("endDate", endDate);
        return this.sqlSession.selectList(this.sqlId("getInFarmBoarId3"), map);
    }
    /* public Integer isOutFarm(BigInteger id, BigInteger pigId, Date eventAt, Long farmId, Date time){
         Map<String, Object> map = new HashMap<>();
         map.put("id",id);
         map.put("pigId",pigId);
         map.put("eventAt",eventAt);
         map.put("farmId",farmId);
         map.put("time",time);
         return this.sqlSession.selectOne(this.sqlId("isOutFarm"), map);
     }*/
    public BigInteger isBarn(BigInteger id, BigInteger pigId, Date eventAt,  Date time){
        Map<String, Object> map = new HashMap<>();
        map.put("id",id);
        map.put("pigId",pigId);
        map.put("eventAt",eventAt);
        map.put("time",time);
        return this.sqlSession.selectOne(this.sqlId("isBarn"), map);
    }
    public Map<String,Object> findBarn(BigInteger isBarn,BigInteger id, BigInteger pigId, Date eventAt, Date time,String operatorName,Long barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("id",id);
        map.put("pigId",pigId);
        map.put("eventAt",eventAt);
        map.put("time",time);
        map.put("operatorName",operatorName);
        map.put("barnId",barnId);
        map.put("isBarn",isBarn);
        return this.sqlSession.selectOne(this.sqlId("findBarn"), map);
    }
    /* public Map<String,Object> findPigInfo(Long pigId){
         Map<String, Object> map = new HashMap<>();
         map.put("pigId",pigId);
         return this.sqlSession.selectOne(this.sqlId("findPigInfo"), map);
     }*/

    /*public Map<String,Object> frontEventId(BigInteger pigId,Date time){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("time",time);
        return this.sqlSession.selectOne(this.sqlId("frontEventId"), map);
    }*/
    public Map<String,Object> frontEvent(Integer parity,BigInteger pigId, Date time ,Integer pigStatus){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("parity",parity);
        map.put("time",time);
        map.put("pigStatus",pigStatus);
        return this.sqlSession.selectOne(this.sqlId("frontEvent"), map);
    }
    public int getPregCheckResult(Integer parity,BigInteger pigId, Date time ,Integer pigStatus){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("parity",parity);
        map.put("time",time);
        map.put("pigStatus",pigStatus);
        return this.sqlSession.selectOne(this.sqlId("getPregCheckResult"), map);
    }
    public Map<String,Object> findBarns(BigInteger pigId,String operatorName,Long barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("operatorName",operatorName);
        map.put("barnId",barnId);
        return this.sqlSession.selectOne(this.sqlId("findBarns"), map);
    }

    public List<Map<String,Object>> getdaizaishu(BigInteger pigId,Date time,Date nearDeliverDate){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("time",time);
        map.put("nearDeliverDate",nearDeliverDate);
        return this.sqlSession.selectList(this.sqlId("getdaizaishu"), map);
    }
    public Map<String,Object> afterEvent(BigInteger pigId,Date time){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("time",time);
        return this.sqlSession.selectOne(this.sqlId("afterEvent"), map);
    }
    public Map<String,Object> nearDeliver(BigInteger pigId,Date time){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("time",time);
        return this.sqlSession.selectOne(this.sqlId("nearDeliver"), map);
    }

    public BigInteger isBoarBarn(BigInteger id, BigInteger pigId, Date eventAt, Date queryDate, Long farmId){
        Map<String, Object> map = new HashMap<>();
        map.put("id",id);
        map.put("pigId",pigId);
        map.put("eventAt",eventAt);
        map.put("queryDate",queryDate);
        map.put("farmId",farmId);
        return this.sqlSession.selectOne(this.sqlId("isBoarBarn"), map);
    }

    public Map<String,Object> findBoarBarn(BigInteger isBoarBarn,BigInteger id,BigInteger pigId,Date eventAt,Date queryDate,String staffName,Integer barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("isBoarBarn",isBoarBarn);
        map.put("id",id);
        map.put("pigId",pigId);
        map.put("eventAt",eventAt);
        map.put("queryDate",queryDate);
        map.put("staffName",staffName);
        map.put("barnId",barnId);
        return this.sqlSession.selectOne(this.sqlId("findBoarBarn"), map);
    }
    public Map<String,Object> findBoarBarns(BigInteger pigId,String staffName,Integer barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("pigId",pigId);
        map.put("staffName",staffName);
        map.put("barnId",barnId);
        return this.sqlSession.selectOne(this.sqlId("findBoarBarns"), map);
    }
   public Map<String,Object> findBoarBarn1(BigInteger pigId,String staffName,Integer barnId,Long farmId,Date queryDate){
       Map<String, Object> map = new HashMap<>();
       map.put("pigId",pigId);
       map.put("staffName",staffName);
       map.put("barnId",barnId);
       map.put("farmId", farmId);
       map.put("queryDate", queryDate);
       return this.sqlSession.selectOne(this.sqlId("findBoarBarn1"), map);
   }
    public Integer checkBarn(Long barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("barnId",barnId);
        return this.sqlSession.selectOne(this.sqlId("checkBarn"), map);
    }
    public BigInteger isBoarChgFarm(BigInteger id, BigInteger pigId, Date eventAt, Date queryDate, Long farmId){
        Map<String, Object> map = new HashMap<>();
        map.put("id",id);
        map.put("pigId",pigId);
        map.put("eventAt",eventAt);
        map.put("queryDate",queryDate);
        map.put("farmId",farmId);
        return this.sqlSession.selectOne(this.sqlId("isBoarChgFarm"), map);
    }

    public String findStaffName(Long currentBarnId, String staffName,Integer barnId){
        Map<String, Object> map = new HashMap<>();
        map.put("currentBarnId",currentBarnId);
        map.put("staffName",staffName);
        map.put("barnId",barnId);
        return this.sqlSession.selectOne(this.sqlId("findStaffName"), map);
    }

    // 事件中心-比如配种事件、转舍事件，当前状态不应该显示空怀，是返情就显示返情 （陈娟 2018-09-05）
    public DoctorPigEvent getKongHuaiStatus(Long pigId) {
        return getSqlSession().selectOne(sqlId("getKongHuaiStatus"), pigId);
    }

}
