package io.terminus.doctor.event.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.event.ListenedRollbackEvent;
import io.terminus.doctor.event.handler.DoctorEventSelector;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandlers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;

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

//    private final DoctorEventHandlerChainInvocation doctorEventHandlerChainInvocation;

//    private final FlowProcessService flowProcessService;
//
//    private final String sowFlowDefinitionKey;
//
//    private final FlowQueryService flowQueryService;
//
//    @Autowired
//    public DoctorPigEventManager(DoctorEventHandlerChainInvocation doctorEventHandlerChainInvocation,
//                                 FlowProcessService flowProcessService,
//                                 @Value("${flow.definition.key.sow:sow}") String sowFlowDefinitionKey,
//                                 FlowQueryService flowQueryService){
//        this.doctorEventHandlerChainInvocation = doctorEventHandlerChainInvocation;
//        this.flowProcessService = flowProcessService;
//        this.sowFlowDefinitionKey = sowFlowDefinitionKey;
//        this.flowQueryService = flowQueryService;
//    }
//
//    /**
//     * 本地管理 Boar Casual 事件信息管理
//     * @param doctorBasicInputInfoDto
//     * @param extra
//     * @return
//     */
//    @Transactional
//    public Map<String, Object> createCasualPigEvent(DoctorBasicInputInfoDto doctorBasicInputInfoDto,
//                                                    Map<String, Object> extra) throws Exception{
//        Map<String,Object> context = Maps.newHashMap();
//        doctorEventHandlerChainInvocation.invoke(doctorBasicInputInfoDto, extra, context);
//
//        /**
//         * 母猪创建对应的事件流信息
//         */
//        Map<String, Object> ids = OBJECT_MAPPER.readValue(context.get("createEventResult").toString(), JacksonType.MAP_OF_OBJECT);
//        ids.put("contextType","single");
//        ids.put("type", doctorBasicInputInfoDto.getEventType());
//        if(Objects.equals(doctorBasicInputInfoDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
//            Long pigId = Params.getWithConvert(ids, "doctorPigId", a->Long.valueOf(a.toString()));
//
//            if(Objects.equals(doctorBasicInputInfoDto.getEventType(), PigEvent.ENTRY.getKey())){
//                flowProcessService.startFlowInstance(sowFlowDefinitionKey, pigId);
//            }else if(Objects.equals(doctorBasicInputInfoDto.getEventType(), PigEvent.REMOVAL.getKey())){
//                flowProcessService.endFlowInstance(sowFlowDefinitionKey, pigId, true, null);
//            }
//        }
//        return ids;
//    }
//    /**
//     * 批量本地管理 Boar Casual 事件信息管理
//     * @param doctorPigEntryEventDtoList
//     * @return
//     */
//    @Transactional
//    public List<Map<String, Object>> createCasualPigEvent(List<DoctorPigEntryEventDto> doctorPigEntryEventDtoList) throws Exception{
//        List<Map<String,Object>> result= Lists.newArrayList();
//        for (DoctorPigEntryEventDto doctorPigEntryEventDto:doctorPigEntryEventDtoList ){
//            DoctorFarmEntryDto doctorFarmEntryDto=doctorPigEntryEventDto.getDoctorFarmEntryDto();
//            DoctorBasicInputInfoDto doctorBasicInputInfoDto=doctorPigEntryEventDto.getDoctorBasicInputInfoDto();
//            Map<String,Object> context = Maps.newHashMap();
//            Map<String,Object> extra=Maps.newHashMap();
//            BeanMapper.copy(doctorFarmEntryDto,extra);
//            doctorEventHandlerChainInvocation.invoke(doctorPigEntryEventDto.getDoctorBasicInputInfoDto(), extra, context);
//            /**
//             * 母猪创建对应的事件流信息
//             */
//            Map<String, Object> ids = OBJECT_MAPPER.readValue(context.get("createEventResult").toString(), JacksonType.MAP_OF_OBJECT);
//            ids.put("barnId",doctorFarmEntryDto.getBarnId());
//            ids.put("contextType","single");
//            ids.put("type", doctorBasicInputInfoDto.getEventType());
//            if(Objects.equals(doctorBasicInputInfoDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
//                Long pigId = Params.getWithConvert(ids, "doctorPigId", a->Long.valueOf(a.toString()));
//                if(Objects.equals(doctorBasicInputInfoDto.getEventType(), PigEvent.ENTRY.getKey())){
//                    flowProcessService.startFlowInstance(sowFlowDefinitionKey, pigId);
//                }else if(Objects.equals(doctorBasicInputInfoDto.getEventType(), PigEvent.REMOVAL.getKey())){
//                    flowProcessService.endFlowInstance(sowFlowDefinitionKey, pigId, true, null);
//                }
//            }
//            result.add(ids);
//        }
//        return result;
//    }
//    /**
//     * 批量创建普通事件信息内容
//     * @param basicList
//     * @param extra
//     * @return 通过PigId 获取对应的返回结果信息
//     */
//    @Transactional
//    public Map<String, Object> createCasualPigEvents(List<DoctorBasicInputInfoDto> basicList, Map<String,Object> extra){
//        Map<String,Object> result = Maps.newHashMap();
//        basicList.forEach(basic->{
//            Map<String,Object> currentContext = Maps.newHashMap();
//            doctorEventHandlerChainInvocation.invoke(basic, extra, currentContext);
//            result.put(basic.getPigId().toString(), JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(currentContext));
//        });
//        result.put("contextType", "mult");
//        return result;
//    }
//
//    /**
//     * 录入母猪信息管理
//     * @param basic
//     * @param extra
//     * @return
//     */
//    @Transactional
//    @SneakyThrows
//    public Map<String,Object> createSowPigEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra){
//        return createSingleSowEvents(basic, extra);
//    }
//
//    /**
//     * 批量创建Pig事件信息
//     * @param basicInputInfoDtos
//     * @param extra
//     * @return
//     */
//    @Transactional
//    @SneakyThrows
//    public Map<String,Object> createSowEvents(List<DoctorBasicInputInfoDto> basicInputInfoDtos, Map<String, Object> extra){
//        Map<String,Object> results = Maps.newHashMap();
//        basicInputInfoDtos.forEach(dto-> results.put(dto.getPigId().toString(),
//                createSingleSowEvents(dto, extra)));
//        results.put("contextType", "mult");
//        return results;
//    }
//
//    @SneakyThrows
//    private Map<String, Object> createSingleSowEvents(DoctorBasicInputInfoDto basic, Map<String, Object> extra){
//        //发送此事件的母猪id
//        extra.put(EVENT_PIG_ID, basic.getPigId());
//
//        // build data
//        String flowData = JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(ImmutableMap.of(
//                "basic",JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(basic),
//                "extra",JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(extra)));
//
//        // execute
//        Executor executor = flowProcessService.getExecutor(sowFlowDefinitionKey, basic.getPigId());
//
//        //  添加参数信息
//        Map<String, Object> express = Maps.newHashMap();
//        express.put("eventType", basic.getEventType());
//        if(Objects.equals(basic.getEventType(), PigEvent.PREG_CHECK.getKey())){
//            express.put("checkResult", extra.get("checkResult"));
//        }
//
//        // 添加对应的操作方式
//        executor.execute(express, flowData);
//        String flowDataContent = flowQueryService.getFlowProcessQuery().getCurrentProcesses(sowFlowDefinitionKey, basic.getPigId()).get(0).getFlowData();
//        Map<String,String> flowDataMap = OBJECT_MAPPER.readValue(flowDataContent, JacksonType.MAP_OF_STRING);
//        Map<String, Object> results = OBJECT_MAPPER.readValue(flowDataMap.get("createEventResult"), JacksonType.MAP_OF_OBJECT);
//        results.put("contextType", "single");
//        results.put("type", basic.getEventType());
//        return results;
//    }


    @Autowired
    private DoctorPigEventHandlers pigEventHandlers;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    /**
     * 事件处理
     * @param inputDto 事件信息数据
     * @param basic 基础数据
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public List<DoctorRollbackDto> eventHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){
        try {
            DoctorPigEventHandler doctorEventCreateHandler = pigEventHandlers.getEventHandlerMap().get(basic.getEventType());
            doctorEventCreateHandler.preHandle(inputDto, basic);
            List<DoctorEventInfo> doctorEventInfoList = Lists.newArrayList();
            doctorEventCreateHandler.handle(doctorEventInfoList, inputDto, basic);
           // transactionalEventHandle(doctorEventCreateHandler, doctorEventInfoList, inputDto, basic);
            return doctorEventCreateHandler.publishEvent(doctorEventInfoList);
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    /**
     * 批量事件处理
     * @param eventInputs
     * @param basic
     * @return
     */
    @Transactional
    public List<DoctorRollbackDto> batchEventsHandle(List<BasePigEventInputDto> eventInputs, DoctorBasicInputInfoDto basic) {
        try {
            DoctorPigEventHandler handler = pigEventHandlers.getEventHandlerMap().get(basic.getEventType());
            List<DoctorEventInfo> eventInfos = Lists.newArrayList();
            eventInputs.forEach(inputDto -> {
                handler.preHandle(inputDto, basic);
                handler.handle(eventInfos, inputDto, basic);
            });
            return handler.publishEvent(eventInfos);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 校验携带数据正确性，发布事件
     */
    public void  checkAndPublishEvent(List<DoctorRollbackDto> dtos) {
        if (notEmpty(dtos)) {
            checkFarmIdAndEventAt(dtos);
            publishRollbackEvent(dtos);
        }
    }

    //发布事件, 用于更新回滚后操作
    private void publishRollbackEvent(List<DoctorRollbackDto> dtos) {
        coreEventDispatcher.publish(ListenedRollbackEvent.builder().doctorRollbackDtos(dtos).build());
    }

    private void checkFarmIdAndEventAt(List<DoctorRollbackDto> dtos) {
        dtos.forEach(dto -> {
            if (dto.getFarmId() == null || dto.getEventAt() == null) {
                throw new ServiceException("publish.rollback.not.null");
            }
        });
    }

    /**
     * 猪当前可执行事件
     * @param pigStatus 猪状态
     * @param pigType 猪舍类型
     * @return 可执行事件
     */
    public List<PigEvent> selectEvents(PigStatus pigStatus, PigType pigType) {
        return DoctorEventSelector.selectPigEvent(pigStatus, pigType);
    }

}
