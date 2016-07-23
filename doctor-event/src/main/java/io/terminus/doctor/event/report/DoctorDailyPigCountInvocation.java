package io.terminus.doctor.event.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.handler.DoctorEventHandlerChain;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.report.count.DoctorDailyRemovalEventCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-07-20
 * Email:yaoqj@terminus.io
 * Descirbe: 通过Event 统计对应的事件母猪的数量信息
 */
@Component
@Slf4j
public class DoctorDailyPigCountInvocation {

    private final DoctorDailyPigCountChain doctorDailyPigCountChain;

    @Autowired
    public DoctorDailyPigCountInvocation(DoctorDailyPigCountChain doctorDailyPigCountChain){
        this.doctorDailyPigCountChain = doctorDailyPigCountChain;
    }

    /**
     * 统计对应的事件信息
     * @param doctorPigEvents
     * @return
     */
    public DoctorDailyReportDto countPigEvent(List<DoctorPigEvent> doctorPigEvents, Map<String, Object> context){

        DoctorDailyReportDto doctorDailyReportDto = new DoctorDailyReportDto();

        List<DoctorDailyEventCount> doctorDailyEventCounts = doctorDailyPigCountChain.getDoctorDailyEventCounts();

        if(isNull(doctorDailyEventCounts)){
            doctorDailyEventCounts = Lists.newArrayList();
        }

        for (DoctorDailyEventCount doctorDailyEventCount : doctorDailyEventCounts) {

            // filter to execute event
            List<DoctorPigEvent> toExe = doctorDailyEventCount.preDailyEventHandleValidate(doctorPigEvents);

            // execute count result
            doctorDailyEventCount.dailyEventHandle(toExe, doctorDailyReportDto, context);

        }
        return doctorDailyReportDto;
    }

}
