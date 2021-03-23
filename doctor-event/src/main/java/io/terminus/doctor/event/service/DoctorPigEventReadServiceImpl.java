package io.terminus.doctor.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorNpdExportDto;
import io.terminus.doctor.event.dto.DoctorPigSalesExportDto;
import io.terminus.doctor.event.dto.DoctorProfitExportDto;
import io.terminus.doctor.event.dto.DoctorSowParityAvgDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.dto.DoctorSuggestPig;
import io.terminus.doctor.event.dto.DoctorSuggestPigSearch;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
import io.terminus.doctor.event.editHandler.DoctorModifyPigEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigEventHandlers;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 添加pig事件读取信息
 */
@Service
@Slf4j
@RpcProvider
public class DoctorPigEventReadServiceImpl implements DoctorPigEventReadService {

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorGroupEventDao doctorGroupEventDao;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    @Autowired
    private DoctorPigEventManager pigEventManager;

    @Autowired
    private DoctorModifyPigEventHandlers doctorModifyPigEventHandlers;

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Value("${flow.definition.key.sow:sow}")
    private String sowFlowKey;

    @Autowired
    public DoctorPigEventReadServiceImpl(DoctorPigEventDao doctorPigEventDao,
                                         DoctorPigTrackDao doctorPigTrackDao, DoctorGroupEventDao doctorGroupEventDao) {
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    public Response<Map<String, List<Integer>>> queryPigEventByPigStatus(List<Integer> statusList) {
        // TODO 母猪状态流转图 前段显示的方式，数据内容的封装
        return null;
    }

    @Override
    public Response<Paging<DoctorPigEvent>> queryPigDoctorEvents(Long farmId, Long pigId,
                                                                 Integer pageNo, Integer pageSize,
                                                                 Date beginDate, Date endDate) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            if (isNull(pigId)) {
                return Response.fail("query.pigDoctorEvents.fail");
            }
            Map<String, Object> criteria = Maps.newHashMap();
            criteria.put("farmId", farmId);
            criteria.put("pigId", pigId);
            criteria.put("beginDate", beginDate);
            criteria.put("endDate", endDate);
            criteria.put("ordered", 0);
            Paging<DoctorPigEvent> doctorPigEventPaging = doctorPigEventDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria);

            return Response.ok(doctorPigEventPaging);
        } catch (Exception e) {
            log.error("query pig doctor events fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvent.fail");
        }
    }

    @Override
    public Response<DoctorPigEvent> findFirstPigEvent(Long pigId, Date fromDate) {
        try {
            return Response.ok(doctorPigEventDao.findFirstPigEvent(pigId, fromDate));
        } catch (Exception e) {
            log.error("findFirstPigEvent fail, pigId={}, fromDate={}, cause:{}", pigId, fromDate, Throwables.getStackTraceAsString(e));
            return Response.fail("find.first.pig.event.fail");
        }
    }

    @Override
    public Response<Long> countByBarnId(Long barnId) {
        try {
            return Response.ok(doctorPigEventDao.countByBarnId(barnId));
        } catch (Exception e) {
            log.error("count pig event by barnId fail, barnId={}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("count.pig.event.by.barn.id.fail");
        }
    }

    @Override
    public Response<List<Integer>> queryPigEvents(List<Long> pigIds) {
        try {
            //查出所有的事件
            List<PigEvent> pigEvents = Lists.newArrayList(PigEvent.values());
            pigIds.forEach(pigId -> {
                //查猪track信息
                DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
                //查猪当前猪舍
                DoctorBarn doctorBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
                //查当前猪可执行的事件，并和已有事件取交集
                pigEvents.retainAll(pigEventManager.selectEvents(PigStatus.from(doctorPigTrack.getStatus()), PigType.from(doctorBarn.getPigType())));
                //当母猪是配种状态，且配种次数大于3，不给配。
                if (pigEvents.contains(PigEvent.MATING) && Objects.equals(doctorPigTrack.getCurrentMatingCount(), 3)) {
                    pigEvents.remove(PigEvent.MATING);
                }
            });
            return Response.ok(pigEvents.stream().map(PigEvent::getKey).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("query pig events fail, pigId:{}, cause:{}", pigIds, Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvents.fail");
        }
    }

    @Override
    public Response<List<DoctorSuggestPig>> suggestPigsByEvent(Integer eventType, Long farmId, String pigCode, Integer sex) {
        try {
            DoctorSuggestPigSearch suggestPigSearch = pigEventManager.selectPigs(eventType);
            suggestPigSearch.setFarmId(farmId);
            suggestPigSearch.setPigCode(pigCode);
            suggestPigSearch.setSex(sex);
            return Response.ok(doctorPigTrackDao.suggestPigs(suggestPigSearch));
        } catch (Exception e) {
            log.error("suggest.pigs.by.event.failed, eventType:{}, farmId:{}, cause:{}", eventType, farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("suggest.pigs.by.event.failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> queryPigEventById(Long id) {
        try {
            return Response.ok(doctorPigEventDao.findById(id));
        } catch (Exception e) {
            log.error("find pig event by id failed, id is {}, cause by {}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvent.fail");
        }
    }

    @Override
    public Response<List<DoctorSowParityCount>> querySowParityCount(Long pigId) {
        try {
            // 获取母猪Track 信息
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
            checkState(!isNull(doctorPigTrack), "pig.track.not.find");
            checkState(Objects.equals(doctorPigTrack.getPigType(), DoctorPig.PigSex.SOW.getKey()), "count.pigType.error");

            // 获取Pig 所有的 EventId
            Map<Integer, List<DoctorPigEvent>> map = doctorPigEventDao.queryAllEventsByPigId(pigId).stream()
                    .sorted(Comparator.comparing(DoctorPigEvent::getParity))
                    .collect(Collectors.groupingBy(DoctorPigEvent::getParity, Collectors.toList()));
            List<DoctorSowParityCount> doctorSowParityCounts = Lists.newArrayList();
            map.keySet().forEach(parity ->
                    doctorSowParityCounts.add(DoctorSowParityCount.doctorSowParityCountConvert(parity, map.get(parity)))
            );
            return Response.ok(doctorSowParityCounts);
        } catch (IllegalStateException se) {
            log.error("query sow parity illegal state fail, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        } catch (Exception e) {
            log.error("query sow parity fail, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("query.sowParityCount.fail");
        }
    }

    @Override
    public Response<Boolean> validatePigNotInFeed(@NotNull(message = "input.pigIds.empty") String pigIds) {
        try {
            Splitters.COMMA.split(pigIds).forEach(pigId -> {
                Boolean hasEquals = Objects.equals(
                        doctorPigTrackDao.findByPigId(Long.valueOf(pigId)).getStatus(),
                        PigStatus.FEED.getKey());
                checkState(!hasEquals, "pigsState.notFeedValidate.fail");
            });
            return Response.ok(Boolean.TRUE);
        } catch (IllegalStateException se) {
            log.warn("validate pig not in farrowing state illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        } catch (Exception e) {
            log.error("validate pig not in farrowing state fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("validate.pigNotIn.fail");
        }
    }

    @Override
    public Response<Paging<DoctorPigEvent>> queryPigEventsByCriteria(Map<String, Object> criteria, Integer pageNo, Integer pageSize) {
        try {
            PageInfo info = PageInfo.of(pageNo, pageSize);
            return Response.ok(doctorPigEventDao.paging(info.getOffset(), info.getLimit(), criteria));
        } catch (Exception e) {
            log.error("query.pig.event.by.criteria.failed cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pig.event.by.criteria.failed");
        }
    }
    public Response<DoctorPigEvent> getabosum(Map<String, Object> criteria, Integer pageNo, Integer pageSize) {
        try {
            DoctorPigEvent doctorPigEvents = doctorPigEventDao.getabosum(criteria).get(0);
            /*int birthNestAvg = 0;
            int liveCount = 0;
            for(DoctorPigEvent a : doctorPigEvents){
                //birthNestAvg = birthNestAvg + (Integer) a.getExtraMap().get("birthNestAvg");
                liveCount = liveCount+a.getLiveCount();
            }
            Map<String,Integer> map = new HashMap<>();
            map.put("birthNestAvg",birthNestAvg);
            map.put("liveCount",liveCount);*/
            return Response.ok(doctorPigEvents);
        } catch (Exception e) {
            log.error("query.pig.event.by.criteria.failed cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pig.event.by.criteria.failed");
        }
    }
    public Response<DoctorPigEvent> getweansum(Map<String, Object> criteria, Integer pageNo, Integer pageSize) {
        try {
            DoctorPigEvent doctorPigEvents = doctorPigEventDao.getweansum(criteria).get(0);
            /*int birthNestAvg = 0;
            int liveCount = 0;
            for(DoctorPigEvent a : doctorPigEvents){
                //birthNestAvg = birthNestAvg + (Integer) a.getExtraMap().get("birthNestAvg");
                liveCount = liveCount+a.getLiveCount();
            }
            Map<String,Integer> map = new HashMap<>();
            map.put("birthNestAvg",birthNestAvg);
            map.put("liveCount",liveCount);*/
            return Response.ok(doctorPigEvents);
        } catch (Exception e) {
            log.error("query.pig.event.by.criteria.failed cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pig.event.by.criteria.failed");
        }
    }
    public Response<DoctorPigEvent> getfosterssum(Map<String, Object> criteria, Integer pageNo, Integer pageSize) {
        try {
            DoctorPigEvent doctorPigEvents = doctorPigEventDao.getfosterssum(criteria).get(0);
            /*int birthNestAvg = 0;
            int liveCount = 0;
            for(DoctorPigEvent a : doctorPigEvents){
                //birthNestAvg = birthNestAvg + (Integer) a.getExtraMap().get("birthNestAvg");
                liveCount = liveCount+a.getLiveCount();
            }
            Map<String,Integer> map = new HashMap<>();
            map.put("birthNestAvg",birthNestAvg);
            map.put("liveCount",liveCount);*/
            return Response.ok(doctorPigEvents);
        } catch (Exception e) {
            log.error("query.pig.event.by.criteria.failed cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pig.event.by.criteria.failed");
        }
    }

    public Response<DoctorPigEvent> getpigletssum(Map<String, Object> criteria, Integer pageNo, Integer pageSize) {
        try {
            DoctorPigEvent doctorPigEvents = doctorPigEventDao.getpigletssum(criteria).get(0);
            /*int birthNestAvg = 0;
            int liveCount = 0;
            for(DoctorPigEvent a : doctorPigEvents){
                //birthNestAvg = birthNestAvg + (Integer) a.getExtraMap().get("birthNestAvg");
                liveCount = liveCount+a.getLiveCount();
            }
            Map<String,Integer> map = new HashMap<>();
            map.put("birthNestAvg",birthNestAvg);
            map.put("liveCount",liveCount);*/
            return Response.ok(doctorPigEvents);
        } catch (Exception e) {
            log.error("query.pig.event.by.criteria.failed cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pig.event.by.criteria.failed");
        }
    }
    @Override
    public Response<List<DoctorEventOperator>> queryOperators(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorPigEventDao.findOperators(criteria));
        } catch (Exception e) {
            log.error("query.Operator.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.Operator.failed");
        }
    }

    @Override
    public Response<Boolean> isLastEvent(Long pigId, Long eventId) {
        try {
            DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventById(pigId);
            if (!Objects.equals(eventId, lastEvent.getId())) {
                return Response.ok(Boolean.FALSE);
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("find pig event is last event failed, pigId:{}, eventId:{}, cause:{}", pigId, eventId, Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }
    }

    @Override
    public Response<Boolean> isLastManualEvent(Long pigId, Long eventId) {
        try {
            DoctorPigEvent lastEvent = doctorPigEventDao.queryLastManualPigEventById(pigId);
            if (lastEvent == null) {
                return Response.ok(Boolean.FALSE);
            }
            if (!Objects.equals(eventId, lastEvent.getId())) {
                return Response.ok(Boolean.FALSE);
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("find pig event is last manual event failed, pigId:{}, eventId:{}, cause:{}", pigId, eventId, Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }
    }

    @Override
    public RespWithEx<DoctorPigEvent> canRollbackEvent(@NotNull(message = "input.pigId.empty") Long pigId) {
        try {
            DoctorPigEvent pigEvent = doctorPigEventDao.queryLastManualPigEventById(pigId);
            if (pigEvent == null) {
                return RespWithEx.ok(null);
            }
            DoctorModifyPigEventHandler handler = doctorModifyPigEventHandlers.getModifyPigEventHandlerMap().get(pigEvent.getType());
            if (handler.canRollback(pigEvent)) {
                return RespWithEx.ok(pigEvent);
            }
            return RespWithEx.ok(null);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("can.rollback.event.failed, cause {}", Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("can.rollback.event.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> eventCanRollback(@NotNull(message = "input.eventId.empty") Long eventId) {
        try {
            DoctorPigEvent pigEvent = doctorPigEventDao.findById(eventId);
            DoctorModifyPigEventHandler handler = doctorModifyPigEventHandlers.getModifyPigEventHandlerMap().get(pigEvent.getType());
            if (handler.canRollback(pigEvent)) {
                return RespWithEx.ok(Boolean.TRUE);
            }
            return RespWithEx.ok(Boolean.FALSE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("event.can.rollback.failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("event can rollback failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> lastEvent(@NotNull(message = "input.pigId.empty") Long pigId) {
        try {
            return Response.ok(doctorPigEventDao.queryLastPigEventById(pigId));
        } catch (Exception e) {
            log.error("last.event.failed, cause {}", Throwables.getStackTraceAsString(e));
            return Response.fail("last.event.failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> lastEvent(@NotNull(message = "input.pigIds.empty") List<Long> pigIds) {
        try {
            return Response.ok(doctorPigEventDao.queryLastPigEventByPigIds(pigIds));
        } catch (Exception e) {
            log.error("last.event.failed, cause {}", Throwables.getStackTraceAsString(e));
            return Response.fail("last.event.failed");
        }
    }

    @Override
    public Response<List<DoctorPigEvent>> addWeanEventAfterFosAndPigLets() {
        try {
            return Response.ok(doctorPigEventDao.addWeanEventAfterFosAndPigLets());
        } catch (Exception e) {
            log.error("add.wean.event.after.failed, cause{}", Throwables.getStackTraceAsString(e));
            return Response.fail("add.wean.event.after.failed");
        }
    }

    @Override
    public Response<DoctorSowParityAvgDto> querySowParityAvg(@NotNull(message = "input.pigId.empty") Long pigId) {
        try {
            return Response.ok(BeanMapper.map(doctorPigEventDao.querySowParityAvg(pigId), DoctorSowParityAvgDto.class));
        } catch (Exception e) {
            log.error("query.sow.parity.failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query sow parity failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> findLastEventByType(@NotNull(message = "input.pigId.empty") Long pigId, Integer type) {
        try {
            return Response.ok(doctorPigEventDao.queryLastEventByType(pigId, type));
        } catch (Exception e) {
            log.error("find.last.event.by.type, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find last event by type failed");
        }
    }

    @Override
    public Response<List<Long>> findPigIdsBy(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorPigEventDao.findPigIdsBy(criteria));
        } catch (Exception e) {
            log.error("find.pigIds.by.event, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find pigIds by type failed");
        }
    }

    @Override
    public Response<Paging<DoctorNpdExportDto>> pagingFindNpd(Map<String, Object> map, Integer offset, Integer limit) {
        try {
            PageInfo pageInfo = new PageInfo(offset, limit);
            return Response.ok(doctorPigEventDao.sumNpdWeanEvent(map, pageInfo.getOffset(), pageInfo.getLimit()));
        } catch (Exception e) {
            log.error("find.npd.event, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find npd event fail");
        }
    }

    @Override
    public Response findNpd(Map<String, Object> map, Integer offset, Integer limit) {

        try {
            PageInfo pageInfo = new PageInfo(offset, limit);
            return Response.ok(doctorPigEventDao.sumNpd(map, pageInfo.getOffset(), pageInfo.getLimit()));
        } catch (Exception e) {
            log.error("find.npd.event, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find npd event fail");
        }
    }

    @Override
    public Response<Paging<DoctorPigSalesExportDto>> pagingFindSales(Map<String, Object> map, Integer offset, Integer limit) {

        try {
            PageInfo pageInfo = new PageInfo(offset, limit);
            return Response.ok(doctorPigEventDao.findSalesEvent(map, pageInfo.getOffset(), pageInfo.getLimit()));
        } catch (Exception e) {
            log.error("find.sales.event, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find sales fail");
        }
    }

    @Override
    public Response<List<DoctorPigSalesExportDto>> listFindSales(Map<String, Object> map) {
        try {
            List<DoctorPigSalesExportDto> list = Lists.newArrayList();
            if(map.containsKey("pigTypeId")) {
                List<Integer> pigTypes = Splitters.splitToInteger((String)map.get("pigTypeId"), Splitters.UNDERSCORE);
                if (pigTypes.contains(PigType.FATTEN_PIG.getValue())) {
                    list.addAll(doctorGroupEventDao.findFattenSales(map));
                } else if (pigTypes.contains(PigType.NURSERY_PIGLET.getValue())){
                    list.addAll(doctorGroupEventDao.findCareSales(map));
                } else if(pigTypes.contains(PigType.DELIVER_SOW.getValue())) {
                    list.addAll(doctorGroupEventDao.findNurseSales(map));
                } else if (pigTypes.contains(PigType.RESERVE.getValue())) {
                    list.addAll(doctorGroupEventDao.findReverseSales(map));
                } else {
                    list.addAll(doctorPigEventDao.findSales(map));
                }
            } else {
                list.addAll(doctorPigEventDao.findSales(map));
                list.addAll(doctorGroupEventDao.findFattenSales(map));
                list.addAll(doctorGroupEventDao.findCareSales(map));
                list.addAll(doctorGroupEventDao.findNurseSales(map));
                list.addAll(doctorGroupEventDao.findReverseSales(map));
            }
            return Response.ok(list);
        } catch (Exception e) {
            log.error("list.find.sales.failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("list find sales failed");
        }
    }

    @Override
    public Response<List<DoctorProfitExportDto>> sumProfitAmount(Map<String, Object> map) {
        try {
            map = Params.filterNullOrEmpty(map);
            return Response.ok(doctorPigEventDao.sumProfitPigType(map));
        } catch (Exception e) {
            log.error("find.sum.profit.amount, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find sum profit amount");
        }
    }

    @Override
    public Response<Integer> findLastParity(Long pigId) {
        try {
            return Response.ok(doctorPigEventDao.findLastParity(pigId));
        } catch (Exception e) {
            log.error("find last parity failed, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.last.parity.failed");
        }
    }

    @Override
    public Response<Integer> findUnWeanCountByParity(Long pigId, Integer parity) {
        try {
            return Response.ok(doctorPigEventDao.findUnWeanCountByParity(pigId, parity));
        } catch (Exception e) {
            log.error("find unwean count by parity failed, pigId:{}, parity:{}, cause:{}",
                    pigId, parity, Throwables.getStackTraceAsString(e));
            return Response.fail("find.unwean.count.by.parity.failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> findLastFirstMateEvent(Long pigId) {
        try {
            return Response.ok(doctorPigEventDao.getFirstMateEvent(pigId, new Date()));
        } catch (Exception e) {
            log.error("find last first mate event failed, pidId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.last.first.mate.event.failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> findLastFirstMateEvent(Long pigId, Integer parity) {
        try {
            return Response.ok(doctorPigEventDao.queryLastFirstMate(pigId, parity));
        } catch (Exception e) {
            log.error("find last first mate event failed, pidId:{},parity:{}, cause:{}", pigId, parity, Throwables.getStackTraceAsString(e));
            return Response.fail("find.last.first.mate.event.failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> findFirstMatingBeforePregCheck(Long pigId, Integer parity, Long id) {
        try {
            return Response.ok(doctorPigEventDao.getFirstMatingBeforePregCheck(pigId, parity, id));
        } catch (Exception e) {
            log.error("find first mating before preg check, pigId:{}, parity:{}, id:{}, cause:{}",
                    pigId, parity, id, Throwables.getStackTraceAsString(e));
            return Response.fail("find.first.mating.before.preg.check");
        }
    }

    @Override
    public Response<DoctorPigEvent> findById(Long eventId) {
        try {
            return Response.ok(doctorPigEventDao.findById(eventId));
        } catch (Exception e) {
            log.error("find pig event by id fail, id:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return Response.fail("find pig event by id fail");
        }

    }

    @Override
    public Response<DoctorPigEvent> getFarrowEventByParity(Long pigId, Integer parity) {
        try {
            return Response.ok(doctorPigEventDao.getFarrowEventByParity(pigId, parity));
        } catch (Exception e) {
            log.error("get farrow event by id fail, pigID:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("get farrow event by pigId fail");
        }
    }

    @Override
    public Response<Date> findEventAtLeadToStatus(Long pigId, Integer status) {
        try {
            return Response.ok(doctorPigEventDao.findEventAtLeadToStatus(pigId, status));
        } catch (Exception e) {
            log.error("find event at lead to status failed ,pigId:{}, status:{}, cause:{}",
                    pigId, status, Throwables.getStackTraceAsString(e));
            return Response.fail("find.eventAt.lead.to.status.failed");
        }
    }

    @Override
    public Response<Date> findMateEventToPigId(Long pigId) {
        try {
            return Response.ok(doctorPigEventDao.findMateEventToPigId(pigId));
        } catch (Exception e) {
            log.error("find event at lead to pigId failed ,pigId:{, cause:{}",
                    pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.eventAt.lead.to.pigId.failed");
        }
    }


    @Override
    public Response<List<Long>> findPigIdsByEvent(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorPigEventDao.findPigIdsByEvent(criteria));
        } catch (Exception e) {
            log.error("find.pigIds.by.event, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("event:find pigIds by type failed");
        }
    }

    @Override
    public Response<Date> findFarmSowEventAt(Long pigId, Long farmId) {
        try {
            return Response.ok(doctorPigEventDao.findFarmSowEventAt(pigId, farmId));
        } catch (Exception e) {
            log.error("find event at lead to status failed ,pigId:{}, status:{}, cause:{}",
                    pigId, farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.eventAt.lead.to.status.failed");
        }
    }

    @Override
    public Response<DoctorPigEvent> getKongHuaiStatus(Long pigId) {
        return Response.ok(doctorPigEventDao.getKongHuaiStatus(pigId));
    }

    @Override
    public Map<String,Object> getBranName(Long pigId, Date date) {
        return doctorPigEventDao.getBranName(pigId, date);
    }

}
