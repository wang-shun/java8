package io.terminus.doctor.event.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.constants.DoctorPigSnapshotConstants;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.handler.DoctorEventHandlerChainInvocation;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.model.DoctorRevertLog;
import io.terminus.doctor.workflow.core.Executor;
import io.terminus.doctor.workflow.service.FlowProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪事件信息录入管理过程
 */
@Component
@Slf4j
public class DoctorPigEventManager {

    private final DoctorEventHandlerChainInvocation doctorEventHandlerChainInvocation;

    private final FlowProcessService flowProcessService;

    private final String sowFlowDefinitionKey;

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorPigSnapshotDao doctorPigSnapshotDao;

    private final DoctorRevertLogDao doctorRevertLogDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    @Autowired
    public DoctorPigEventManager(DoctorEventHandlerChainInvocation doctorEventHandlerChainInvocation,
                                 FlowProcessService flowProcessService,DoctorPigEventDao doctorPigEventDao,
                                 DoctorPigSnapshotDao doctorPigSnapshotDao,DoctorRevertLogDao doctorRevertLogDao,
                                 DoctorPigTrackDao doctorPigTrackDao,
                                 @Value("${flow.definition.key.sow:sow}") String sowFlowDefinitionKey){
        this.doctorEventHandlerChainInvocation = doctorEventHandlerChainInvocation;
        this.flowProcessService = flowProcessService;
        this.sowFlowDefinitionKey = sowFlowDefinitionKey;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorRevertLogDao = doctorRevertLogDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
    }

    @Transactional
    public Long rollBackPigEvent(Long pigEventId, Integer revertPigType, Long staffId, String staffName){

        // delete event
        checkState(doctorPigEventDao.delete(pigEventId), "delete.pigEventById.fail");

        // roll back track info
        DoctorPigSnapshot doctorPigSnapshot = doctorPigSnapshotDao.queryByEventId(pigEventId);
        DoctorPigTrack doctorPigTrack =
                JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(
                        String.valueOf(doctorPigSnapshot.getPigInfoMap().get(DoctorPigSnapshotConstants.PIG_TRACK)),
                        DoctorPigTrack.class);
        checkState(doctorPigTrackDao.update(doctorPigTrack), "update.snapshot.fail");

        //delete snapshot
        checkState(doctorPigSnapshotDao.deleteByEventId(pigEventId), "delete.snapshot.error");

        // create roll back log
        DoctorRevertLog doctorRevertLog = DoctorRevertLog.builder()
                .type(revertPigType).fromInfo(pigEventId.toString()).toInfo(pigEventId.toString())
                .reverterId(staffId).reverterName(staffName)
                .build();
        doctorRevertLogDao.create(doctorRevertLog);
        return doctorRevertLog.getId();
    }

    /**
     * 本地管理 Boar Casual 事件信息管理
     * @param doctorBasicInputInfoDto
     * @param extra
     * @return
     */
    @Transactional
    public Map<String, Object> createCasualPigEvent(DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                                    Map<String, Object> extra){
        Map<String,Object> context = Maps.newHashMap();
        doctorEventHandlerChainInvocation.invoke(doctorBasicInputInfoDto, extra, context);
        return context;
    }

    /**
     * 批量创建普通事件信息内容
     * @param basicList
     * @param extra
     * @return 通过PigId 获取对应的返回结果信息
     */
    public Map<String, Object> createCasualPigEvents(List<DoctorBasicInputInfoDto> basicList, Map<String,Object> extra){
        Map<String,Object> result = Maps.newHashMap();
        basicList.forEach(basic->{
            Map<String,Object> currentContext = Maps.newHashMap();
            doctorEventHandlerChainInvocation.invoke(basic, extra, currentContext);
            result.put(basic.getPigId().toString(), JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(currentContext));
        });
        return result;
    }

    /**
     * 录入母猪信息管理
     * @param basic
     * @param extra
     * @return
     */
    @Transactional
    public Map<String,Object> createSowPigEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra){
        // build data
        String flowData = JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(ImmutableMap.of(
                "basic",JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(basic),
                "dto",JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(extra)));

        // execute
        flowProcessService.startFlowInstance(sowFlowDefinitionKey, basic.getPigId());   // 启动流程实例
        Executor executor = flowProcessService.getExecutor(sowFlowDefinitionKey, basic.getPigId());

        Map<String,String> express = Maps.newHashMap();
        express.put("eventType", basic.getEventType().toString());

        // 添加对应的操作方式
        executor.execute(express, flowData);

        // TODO wait workflow context
        // TODO from executor  取出对应的结果信息返回操作方式
        String flowDataContent = "";

        return Maps.newHashMap(); // TODO
    }
}
