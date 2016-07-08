package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeEventQuery;
import io.terminus.doctor.workflow.service.FlowQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private final FlowQueryService flowQueryService;

    @Value("${flow.definition.key.sow:sow}")
    private String sowFlowKey;

    @Autowired
    public DoctorPigEventReadServiceImpl(DoctorPigEventDao doctorPigEventDao,
                                         FlowQueryService flowQueryService){
        this.doctorPigEventDao = doctorPigEventDao;
        this.flowQueryService = flowQueryService;
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
}
