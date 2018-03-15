package io.terminus.doctor.event.reportBi.listener;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.event.reportBi.DoctorReportBiDataSynchronize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Slf4j
@Component
public class DoctorReportBiListener implements EventListener{

    private final DoctorReportBiDataSynchronize synchronize;

    @Autowired
    public DoctorReportBiListener(DoctorReportBiDataSynchronize synchronize) {
        this.synchronize = synchronize;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void synchronizeRealTimeData(DoctorReportBiReaTimeEvent reportBiReaTimeEvent) {
        log.info("synchronize real time data listen, orzId:{}, orzType:{}", reportBiReaTimeEvent.getOrzId(), reportBiReaTimeEvent.getOrzType());
        synchronize.synchronizeRealTimeBiData(reportBiReaTimeEvent.getOrzId(), reportBiReaTimeEvent.getOrzType());
    }

}
