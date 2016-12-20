package io.terminus.doctor.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorSowParityAvgDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeEventQuery;
import io.terminus.doctor.workflow.service.FlowQueryService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private final FlowQueryService flowQueryService;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    @Value("${flow.definition.key.sow:sow}")
    private String sowFlowKey;

    @Autowired
    public DoctorPigEventReadServiceImpl(DoctorPigEventDao doctorPigEventDao,
                                         FlowQueryService flowQueryService,
                                         DoctorPigTrackDao doctorPigTrackDao){
        this.doctorPigEventDao = doctorPigEventDao;
        this.flowQueryService = flowQueryService;
        this.doctorPigTrackDao = doctorPigTrackDao;
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
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            if(isNull(farmId) || isNull(pigId)){
                return Response.fail("query.pigDoctorEvents.fail");
            }
            Map<String,Object> criteria = Maps.newHashMap();
            criteria.put("farmId", farmId);
            criteria.put("pigId", pigId);
            criteria.put("beginDate",beginDate);
            criteria.put("endDate", endDate);
            criteria.put("ordered", 0);
            Paging<DoctorPigEvent> doctorPigEventPaging = doctorPigEventDao.paging(pageInfo.getOffset(),pageInfo.getLimit(), criteria);

            return Response.ok(doctorPigEventPaging);
        }catch (Exception e){
            log.error("query pig doctor events fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvent.fail");
        }
    }

    @Override
    public Response<DoctorPigEvent> findFirstPigEvent(Long pigId, Date fromDate){
        try{
            return Response.ok(doctorPigEventDao.findFirstPigEvent(pigId, fromDate));
        }catch (Exception e) {
            log.error("findFirstPigEvent fail, pigId={}, fromDate={}, cause:{}", pigId, fromDate, Throwables.getStackTraceAsString(e));
            return Response.fail("find.first.pig.event.fail");
        }
    }

    @Override
    public Response<Long> countByBarnId(Long barnId){
        try{
            return Response.ok(doctorPigEventDao.countByBarnId(barnId));
        }catch (Exception e) {
            log.error("count pig event by barnId fail, barnId={}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("count.pig.event.by.barn.id.fail");
        }
    }

    @Override
    public Response<List<Integer>> queryPigEvents(List<Long> pigIds) {
        try{
            checkState(!isNull(pigIds) && !Iterables.isEmpty(pigIds), "input.pigIds.empty");
            FlowDefinitionNodeEventQuery definitionNodeDao = flowQueryService.getFlowDefinitionNodeEventQuery();

            Set<Integer> collectExecute = Sets.newHashSet();

            collectExecute.addAll(
                    definitionNodeDao.getNextTaskNodeEvents(sowFlowKey, pigIds.get(0)).stream()
                    .map(s->Integer.valueOf(s.getValue()))
                    .collect(Collectors.toList()));

            for(int i = 1; i<pigIds.size(); i++){
                Long pigId = pigIds.get(i);

                collectExecute.retainAll(definitionNodeDao.getNextTaskNodeEvents(sowFlowKey, pigId).stream()
                        .map(s->Integer.valueOf(s.getValue()))
                        .collect(Collectors.toList()));
            }

            // remove FOSTERS_BY
            collectExecute.remove(PigEvent.FOSTERS_BY.getKey());
            return Response.ok(Lists.newArrayList(collectExecute));
        }catch (Exception e){
            log.error("query pig events fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvents.fail");
        }
    }

    @Override
    public Response<DoctorPigEvent> queryPigEventById(Long id) {
        try{
            return Response.ok(doctorPigEventDao.findById(id));
        } catch (Exception e) {
            log.error("find pig event by id failed, id is {}, cause by {}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvent.fail");
        }
    }

    @Override
    public Response<List<DoctorSowParityCount>> querySowParityCount(Long pigId) {
        try{
            // 获取母猪Track 信息
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
            checkState(!isNull(doctorPigTrack), "input.pigIdCode.error");
            checkState(Objects.equals(doctorPigTrack.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey()), "count.pigType.error");

            // 获取Pig 所有的 EventId
            Map<Integer, List<DoctorPigEvent>> map = doctorPigEventDao.queryAllEventsByPigId(pigId).stream()
                    .sorted(Comparator.comparing(DoctorPigEvent::getParity))
                    .collect(Collectors.groupingBy(k -> k.getParity(), Collectors.toList()));
            List<DoctorSowParityCount> doctorSowParityCounts = Lists.newArrayList();
            map.keySet().forEach(parity ->
                    doctorSowParityCounts.add(DoctorSowParityCount.doctorSowParityCountConvert(parity, map.get(parity)))
            );
            return Response.ok(doctorSowParityCounts);
        }catch (IllegalStateException se){
            log.warn("query sow parity illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("query sow parity fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.sowParityCount.fail");
        }
    }

    @Override
    public Response<Boolean> validatePigNotInFeed(@NotNull(message = "input.pigIds.empty") String pigIds) {
        try{
            Splitters.COMMA.split(pigIds).forEach(pigId->{
                Boolean hasEquals = Objects.equals(
                        doctorPigTrackDao.findByPigId(Long.valueOf(pigId)).getStatus(),
                        PigStatus.FEED.getKey());
                checkState(!hasEquals, "pigsState.notFeedValidate.fail");
            });
        	return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException se){
            log.warn("validate pig not in farrowing state illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("validate pig not in farrowing state fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("validate.pigNotIn.fail");
        }
    }

    /**
     * 通过PigRel EventId
     * @param pigRelEventId
     * @return
     */
    @SneakyThrows
    private static Map<Integer, List<Long>> convertPigRelEventId(String pigRelEventId){
        Map<String, String> parityEventIds = OBJECT_MAPPER.readValue(pigRelEventId, JacksonType.MAP_OF_STRING);
        return parityEventIds.entrySet().stream()
                .collect(Collectors.toMap(
                        k -> Integer.valueOf(k.getKey()),
                        v -> Splitters.COMMA.splitToList(v.getValue())
                                .stream().map(s->Long.valueOf(s)).collect(Collectors.toList())));
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

    @Override
    public Response<List<DoctorPigEvent>> queryOperators(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorPigEventDao.findOperators(criteria));
        } catch (Exception e) {
            log.error("query.Operator.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.Operator.failed");
        }
    }

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
    public Response<DoctorPigEvent> canRollbackEvent(@NotNull(message = "input.pigId.empty") Long pigId) {
        try {
            return Response.ok(doctorPigEventDao.canRollbackEvent(ImmutableMap.of("pigId", pigId, "isAuto", IsOrNot.NO.getValue(), "beginDate", DateTime.now().minusMonths(3).toDate())));
            } catch (Exception e) {
            log.error("can.rollback.event.failed, cause {}", Throwables.getStackTraceAsString(e));
            return Response.fail("can.rollback.event.failed");
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
            DoctorSowParityAvgDto doctorSowParityAvgDto = new DoctorSowParityAvgDto();
            Map<String, Object> resultMap = doctorPigEventDao.querySowParityAvg(pigId);
            if (resultMap != null) {
                doctorSowParityAvgDto = BeanMapper.map(resultMap, DoctorSowParityAvgDto.class);
            }
            return Response.ok(doctorSowParityAvgDto);
        } catch (Exception e) {
            log.error("query.sow.parity.failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query sow parity failed");
        }
    }

    private Object nullToZero(Object value) {
        return value == null ? 0 : value;
    }

}
