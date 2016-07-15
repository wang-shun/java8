package io.terminus.doctor.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeEventQuery;
import io.terminus.doctor.workflow.service.FlowQueryService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
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
            return Response.ok(doctorPigEventDao.paging(pageInfo.getOffset(),pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("query pig doctor events fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvent.fail");
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

            // 获取胎次关联的 PigId : eventIds
            String relEventIds = doctorPigTrack.getRelEventIds();

            // 获取Pig 所有的 EventId
            Map<Long, DoctorPigEvent> doctorPigEventMap = doctorPigEventDao.queryAllEventsByPigId(pigId)
                    .stream().collect(Collectors.toMap(k->k.getId(), v->v));

            Map<Integer, List<Long>> pigRelEventIds = convertPigRelEventId(relEventIds);

            List<DoctorSowParityCount> doctorSowParityCounts = pigRelEventIds.entrySet().stream()
                    .map(s -> DoctorSowParityCount.doctorSowParityCountConvert(
                            s.getKey(),
                            s.getValue().stream().map(v -> doctorPigEventMap.get(v)).collect(Collectors.toList())))
                    .collect(Collectors.toList());

            doctorSowParityCounts.sort((a, b)->b.getParity().compareTo(a.getParity()));
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
}
