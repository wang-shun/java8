package io.terminus.doctor.event.event;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.search.barn.BarnSearchWriteService;
import io.terminus.doctor.event.search.group.GroupSearchWriteService;
import io.terminus.doctor.event.search.pig.PigSearchWriteService;
import io.terminus.doctor.event.service.DoctorDailyGroupReportWriteService;
import io.terminus.doctor.event.service.DoctorDailyPigReportWriteService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.event.service.DoctorRollbackService;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Desc: 猪/猪群/猪舍 信息修改刷新ES事件监听
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Component
@Slf4j
public class DoctorZKListener implements EventListener {

    @Autowired(required = false)
    private Subscriber subscriber;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    @Autowired
    private DoctorPigEventReadService doctorPigEventReadService;

    @Autowired
    private DoctorPigReadService doctorPigReadService;

    @Autowired
    private DoctorPigWriteService doctorPigWriteService;

    @Autowired
    private PigSearchWriteService pigSearchWriteService;

    @Autowired
    private GroupSearchWriteService groupSearchWriteService;

    @Autowired
    private BarnSearchWriteService barnSearchWriteService;

    @Autowired
    private DoctorDailyPigReportWriteService doctorDailyPigReportWriteService;
    
    @Autowired
    private DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    private DoctorDailyGroupReportWriteService doctorDailyGroupReportWriteService;

    @Autowired
    private DoctorRollbackService doctorRollbackService;

    @PostConstruct
    public void subs() {
        try{
            if (subscriber == null) {
                return;
            }
            subscriber.subscribe(data -> {
                DataEvent dataEvent = DataEvent.fromBytes(data);
                if (dataEvent != null && dataEvent.getEventType() != null) {
                    coreEventDispatcher.publish(dataEvent);
                }
            });
        } catch (Exception e) {
            log.error("ZK subscriber failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 处理监听的信息
     */
    @Subscribe
    public void handleEvent(DataEvent dataEvent) {
        log.info("data event data:{}", dataEvent);

        // 1. 如果是猪创建事件信息
        if (DataEventType.PigEventCreate.getKey() == dataEvent.getEventType()) {
            PigEventCreateEvent pigEventCreateEvent = DataEvent.analyseContent(dataEvent, PigEventCreateEvent.class);
            if (pigEventCreateEvent != null && pigEventCreateEvent.getContext() != null) {
                Map<String, Object> context = pigEventCreateEvent.getContext();
                if("single".equals(context.get("contextType"))) {
                    Long pigId = Params.getWithConvert(context, "doctorPigId", d -> Long.valueOf(d.toString()));
                    pigSearchWriteService.update(pigId);
                }else {
                    context.remove("contextType");
                    context.keySet().forEach(s -> pigSearchWriteService.update(Long.valueOf(s)));
                }
                // 发送 PigEventCreateEvent 事件
                coreEventDispatcher.publish(pigEventCreateEvent);
            }
            return;
        }

        // 2. 如果是猪群信息修改
        if (DataEventType.GroupEventCreate.getKey() == dataEvent.getEventType()) {
            Map<String, Serializable> context = DataEvent.analyseContent(dataEvent, Map.class);
            Long groupId = Params.getWithConvert(context, "doctorGroupId", d -> Long.valueOf(d.toString()));

            // update es index
            groupSearchWriteService.update(groupId);

            //更新猪群统计信息
            groupDailyReportUpdate(context);
            return;
        }

        // 3. 如果是猪舍信息修改
        if (DataEventType.BarnUpdate.getKey() == dataEvent.getEventType()) {
            Map<String, Serializable> context = DataEvent.analyseContent(dataEvent, Map.class);
            Long barnId = Params.getWithConvert(context, "doctorBarnId", d -> Long.valueOf(d.toString()));
            // update es index
            barnSearchWriteService.update(barnId);
            return;
        }

        // 4. 如果是事件回滚
        if (DataEventType.RollBackReport.getKey() == dataEvent.getEventType()) {
            doctorRollbackService.rollbackReportAndES(DataEvent.analyseContent(dataEvent, List.class));
        }
    }

    /**
     * 监听猪事件
     * @param pigEventCreateEvent
     */
    @Subscribe
    public void handlePigEvent(PigEventCreateEvent pigEventCreateEvent) {
        if (pigEventCreateEvent != null && pigEventCreateEvent.getContext() != null) {
            Map<String, Object> context = pigEventCreateEvent.getContext();
            if("single".equals(context.get("contextType"))) {
                Long pigId = Params.getWithConvert(context, "doctorPigId", d -> Long.valueOf(d.toString()));
                Long doctorEventId = Params.getWithConvert(context, "doctorEventId", d -> Long.valueOf(d.toString()));
                updateTrackExtraMessage(pigId, doctorEventId);

                // add event daily reduce
                pigDailyReportUpdate(doctorEventId);
            }else {
                context.remove("contextType");
                context.values().forEach(inContext -> {
                    if (inContext != null) {
                        Map inContextMap = (Map) inContext;
                        Long pigId = Params.getWithConvert(inContextMap, "doctorPigId", d -> Long.valueOf(d.toString()));
                        Long doctorEventId = Params.getWithConvert(inContextMap, "doctorEventId", d -> Long.valueOf(d.toString()));
                        updateTrackExtraMessage(pigId, doctorEventId);

                        // add event daily
                        pigDailyReportUpdate(doctorEventId);
                    }
                });
            }
        }
    }

    private void pigDailyReportUpdate(Long eventId){
        Response<Boolean> response = doctorDailyPigReportWriteService.updateDailyPigReportInfo(eventId);
        if(! response.isSuccess()){
            log.error("update daily pig report error, cause:{}", response.getError());
        }
    }

    //猪群的统计
    private void groupDailyReportUpdate(Map<String, Serializable> context) {
        Long orgId = Params.getWithConvert(context, "doctorOrgId", d -> Long.valueOf(d.toString()));
        Long farmId = Params.getWithConvert(context, "doctorFarmId", d -> Long.valueOf(d.toString()));
        Long eventId = Params.getWithConvert(context, "doctorGroupEventId", d -> Long.valueOf(d.toString()));
        
        //更新数据库的存栏统计
        doctorPigTypeStatisticWriteService.statisticGroup(orgId, farmId);
        
        //更新日报缓存
        doctorDailyGroupReportWriteService.updateDailyGroupReportCache(eventId);
    }

    /**
     * 当猪触发事件之后, 清除 extra_message 中对应的 event 类型的数据.
     * @param pigId
     * @param doctorEventId
     */
    private void updateTrackExtraMessage(Long pigId, Long doctorEventId) {
        DoctorPigTrack pigTrack = RespHelper.orServEx(doctorPigReadService.findPigTrackByPigId(pigId));
        DoctorPig pig = RespHelper.orServEx(doctorPigReadService.findPigById(pigId));
        DoctorPigEvent doctorPigEvent = RespHelper.orServEx(doctorPigEventReadService.queryPigEventById(doctorEventId));
        if (pigTrack != null && doctorPigEvent != null && pig != null) {
            List<DoctorPigMessage> tempMessageList = Lists.newArrayList();
            pigTrack.setExtraMessage(pigTrack.getExtraMessage());
            // 去除当前事件执行后的消息提示
            pigTrack.getExtraMessageList().forEach(doctorPigMessage -> {
                if (!Objects.equals(doctorPigMessage.getEventType(), doctorPigEvent.getType())) {
                    tempMessageList.add(doctorPigMessage);
                }
            });
            pigTrack.setExtraMessageList(tempMessageList);
            doctorPigWriteService.updatePigTrackExtraMessage(pigTrack);

            // 统计数据
            doctorPigTypeStatisticWriteService.statisticPig(pig.getOrgId(), pig.getFarmId(), pig.getPigType());
        }
    }
}
