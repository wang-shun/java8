package io.terminus.doctor.event.event;

import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.event.search.barn.BarnSearchWriteService;
import io.terminus.doctor.event.search.group.GroupSearchWriteService;
import io.terminus.doctor.event.search.pig.PigSearchWriteService;
import io.terminus.doctor.event.service.DoctorDailyGroupReportWriteService;
import io.terminus.doctor.event.service.DoctorDailyPigReportWriteService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import io.terminus.doctor.event.service.DoctorRollbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 16/11/9.
 * 事件监听
 */
@Slf4j
@Component
public class DoctorEventListener implements EventListener{

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

    /**
     * 监听处理多个猪事件
     * @param listenedPigEvents
     */
    @Subscribe
    public void handlePigEvents(ListenedPigEvents listenedPigEvents){
        try {
            log.info("[DoctorEventListener]-> handle.pig.events, listenedPigEvents->{}", listenedPigEvents);
            listenedPigEvents.getList().forEach(listenedPigEvent -> {
                pigSearchWriteService.update(listenedPigEvent.getPigId());
                pigDailyReportUpdate(listenedPigEvent.getPigEventId());
            });
        } catch (Exception e) {
            log.error("[DoctorEventListener]-> handle.pig.events.failed, cause {}, listenedPigEvents->{}", Throwables.getStackTraceAsString(e), listenedPigEvents);
        }
    }

    /**
     * 监听处理猪事件
     * @param listenedPigEvent
     */
    @Subscribe
    public void handlePigEvent(ListenedPigEvent listenedPigEvent){
        try {
            log.info("[DoctorEventListener]-> handle.pig.event, listenedPigEvent->{}", listenedPigEvent);
                pigSearchWriteService.update(listenedPigEvent.getPigId());
                pigDailyReportUpdate(listenedPigEvent.getPigEventId());
        } catch (Exception e) {
            log.error("[DoctorEventListener]-> handle.pig.event.failed, cause {}, listenedPigEvent->{}", Throwables.getStackTraceAsString(e), listenedPigEvent);
        }
    }

    /**
     * 处理监听到的猪群事件
     * @param listenedGroupEvent
     */
    @Subscribe
    public void handleGroupEvent(ListenedGroupEvent listenedGroupEvent){
        try {
            log.info("[DoctorEventListener]-> handle.group.event, listenedGroupEvent->{}", listenedGroupEvent);
            // update es index
            groupSearchWriteService.update(listenedGroupEvent.getGroupId());

            //更新猪群统计信息
            groupDailyReportUpdate(listenedGroupEvent);
        } catch (Exception e) {
            log.error("[DoctorEventListener]-> handle.group.event.failed, cause {}, listenedGroupEvent->{}", Throwables.getStackTraceAsString(e), listenedGroupEvent);
        }
    }

    /**
     * 监听处理有关猪舍的事件
     * @param listenedBarnEvent
     */
    @Subscribe
    public void handleBarnEvent(ListenedBarnEvent listenedBarnEvent){
        try {
            log.info("[DoctorEventListener]-> handle.barn.event, listenedBarnEvent->{}", listenedBarnEvent);
            barnSearchWriteService.update(listenedBarnEvent.getBarnId());
        } catch (Exception e) {
            log.error("[DoctorEventListener]-> handle.barn.event.failed, cause {}, listenedBarnEvent->{}", Throwables.getStackTraceAsString(e), listenedBarnEvent);
        }

    }

    /**
     * 监听处理回滚事件
     * @param listenedRollbackEvent
     */
    @Subscribe
    public void handleRollbackEvent(ListenedRollbackEvent listenedRollbackEvent){
        try {
            log.info("[DoctorEventListener]-> handle.rollback.event, listenedRollbackEvent->{}", listenedRollbackEvent);
            doctorRollbackService.rollbackReportAndES(listenedRollbackEvent.getDoctorRollbackDtos());
        } catch (Exception e) {
            log.error("[DoctorEventListener]-> handle.rollback.event.failed, cause {}, listenedRollbackEvent->{}", Throwables.getStackTraceAsString(e), listenedRollbackEvent);
        }
    }

    private void pigDailyReportUpdate(Long eventId){
        Response<Boolean> response = doctorDailyPigReportWriteService.updateDailyPigReportInfo(eventId);
        if(! response.isSuccess()){
            log.error("update daily pig report error, cause:{}", response.getError());
        }
    }

    //猪群的统计
    private void groupDailyReportUpdate(ListenedGroupEvent listenedGroupEvent) {
        Long orgId = listenedGroupEvent.getOrgId();
        Long farmId = listenedGroupEvent.getFarmId();
        Long eventId = listenedGroupEvent.getDoctorGroupEventId();
        //更新数据库的存栏统计
        doctorPigTypeStatisticWriteService.statisticGroup(orgId, farmId);

        //更新日报缓存
        doctorDailyGroupReportWriteService.updateDailyGroupReportCache(eventId);
    }
}
