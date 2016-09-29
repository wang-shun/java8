package io.terminus.doctor.event.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.handler.DoctorEventHandlerChainInvocation;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Executor;
import io.terminus.doctor.workflow.service.FlowProcessService;
import io.terminus.doctor.workflow.service.FlowQueryService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.doctor.event.constants.DoctorPigExtraConstants.EVENT_PIG_ID;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪事件信息录入管理过程
 */
@Component
@Slf4j
public class DoctorPigEventManager {

    private final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private final DoctorEventHandlerChainInvocation doctorEventHandlerChainInvocation;

    private final FlowProcessService flowProcessService;

    private final String sowFlowDefinitionKey;

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorPigSnapshotDao doctorPigSnapshotDao;

    private final DoctorRevertLogDao doctorRevertLogDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final FlowQueryService flowQueryService;

    @Autowired
    public DoctorPigEventManager(DoctorEventHandlerChainInvocation doctorEventHandlerChainInvocation,
                                 FlowProcessService flowProcessService,DoctorPigEventDao doctorPigEventDao,
                                 DoctorPigSnapshotDao doctorPigSnapshotDao,DoctorRevertLogDao doctorRevertLogDao,
                                 DoctorPigTrackDao doctorPigTrackDao,
                                 @Value("${flow.definition.key.sow:sow}") String sowFlowDefinitionKey,
                                 FlowQueryService flowQueryService){
        this.doctorEventHandlerChainInvocation = doctorEventHandlerChainInvocation;
        this.flowProcessService = flowProcessService;
        this.sowFlowDefinitionKey = sowFlowDefinitionKey;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorRevertLogDao = doctorRevertLogDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.flowQueryService = flowQueryService;
    }

    /**
     * 本地管理 Boar Casual 事件信息管理
     * @param doctorBasicInputInfoDto
     * @param extra
     * @return
     */
    @Transactional
    public Map<String, Object> createCasualPigEvent(DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                                    Map<String, Object> extra) throws Exception{
        Map<String,Object> context = Maps.newHashMap();
        doctorEventHandlerChainInvocation.invoke(doctorBasicInputInfoDto, extra, context);

        /**
         * 母猪创建对应的事件流信息
         */
        Map<String, Object> ids = OBJECT_MAPPER.readValue(context.get("createEventResult").toString(), JacksonType.MAP_OF_OBJECT);
        ids.put("contextType","single");
        ids.put("type", doctorBasicInputInfoDto.getEventType());
        if(Objects.equals(doctorBasicInputInfoDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
            Long pigId = Params.getWithConvert(ids, "doctorPigId", a->Long.valueOf(a.toString()));

            if(Objects.equals(doctorBasicInputInfoDto.getEventType(), PigEvent.ENTRY.getKey())){
                flowProcessService.startFlowInstance(sowFlowDefinitionKey, pigId);
            }else if(Objects.equals(doctorBasicInputInfoDto.getEventType(), PigEvent.REMOVAL.getKey())){
                flowProcessService.endFlowInstance(sowFlowDefinitionKey, pigId, true, null);
            }
        }
        return ids;
    }

    /**
     * 批量创建普通事件信息内容
     * @param basicList
     * @param extra
     * @return 通过PigId 获取对应的返回结果信息
     */
    @Transactional
    public Map<String, Object> createCasualPigEvents(List<DoctorBasicInputInfoDto> basicList, Map<String,Object> extra){
        Map<String,Object> result = Maps.newHashMap();
        basicList.forEach(basic->{
            Map<String,Object> currentContext = Maps.newHashMap();
            doctorEventHandlerChainInvocation.invoke(basic, extra, currentContext);
            result.put(basic.getPigId().toString(), JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(currentContext));
        });
        result.put("contextType", "mult");
        return result;
    }

    /**
     * 录入母猪信息管理
     * @param basic
     * @param extra
     * @return
     */
    @Transactional
    @SneakyThrows
    public Map<String,Object> createSowPigEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra){
        return createSingleSowEvents(basic, extra);
    }

    /**
     * 批量创建Pig事件信息
     * @param basicInputInfoDtos
     * @param extra
     * @return
     */
    @Transactional
    @SneakyThrows
    public Map<String,Object> createSowEvents(List<DoctorBasicInputInfoDto> basicInputInfoDtos, Map<String, Object> extra){
        Map<String,Object> results = Maps.newHashMap();
        basicInputInfoDtos.forEach(dto-> results.put(dto.getPigId().toString(),
                createSingleSowEvents(dto, extra)));
        results.put("contextType", "mult");
        return results;
    }

    @SneakyThrows
    private Map<String, Object> createSingleSowEvents(DoctorBasicInputInfoDto basic, Map<String, Object> extra){

        checkMatingCount(basic, extra);

        //发送此事件的母猪id
        extra.put(EVENT_PIG_ID, basic.getPigId());

        // build data
        String flowData = JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(ImmutableMap.of(
                "basic",JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(basic),
                "extra",JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(extra)));

        // execute
        Executor executor = flowProcessService.getExecutor(sowFlowDefinitionKey, basic.getPigId());

        //  添加参数信息
        Map<String, Object> express = Maps.newHashMap();
        express.put("eventType", basic.getEventType());
        if(Objects.equals(basic.getEventType(), PigEvent.PREG_CHECK.getKey())){
            express.put("checkResult", extra.get("checkResult"));
        }

        // 添加对应的操作方式
        executor.execute(express, flowData);
        String flowDataContent = flowQueryService.getFlowProcessQuery().getCurrentProcesses(sowFlowDefinitionKey, basic.getPigId()).get(0).getFlowData();
        Map<String,String> flowDataMap = OBJECT_MAPPER.readValue(flowDataContent, JacksonType.MAP_OF_STRING);
        Map<String, Object> results = OBJECT_MAPPER.readValue(flowDataMap.get("createEventResult"), JacksonType.MAP_OF_OBJECT);
        results.put("contextType", "single");
        results.put("type", basic.getEventType());
        return results;
    }

    @SneakyThrows
    private void checkMatingCount(DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        // 如果妊娠检查非阳性 或 转入配种舍,置当前配种数为0
        if ((extra.containsKey("checkResult") && !Objects.equals(extra.get("checkResult"), PregCheckResult.YANG.getKey())) ||
                Objects.equals(basic.getEventType(), PigEvent.TO_MATING.getKey())) {
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(basic.getPigId());
            doctorPigTrack.setCurrentMatingCount(0);
            doctorPigTrackDao.update(doctorPigTrack);
        }
        // 重复配种就加次数
        if (Objects.equals(basic.getEventType(), PigEvent.MATING.getKey())) {
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(basic.getPigId());
            doctorPigTrack.setCurrentMatingCount(doctorPigTrack.getCurrentMatingCount() + 1);
            doctorPigTrackDao.update(doctorPigTrack);
        }
    }
}
