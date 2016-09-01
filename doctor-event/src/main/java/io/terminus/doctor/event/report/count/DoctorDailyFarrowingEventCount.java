package io.terminus.doctor.event.report.count;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeliverDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 分娩日报数据统计
 */
@Component
public class DoctorDailyFarrowingEventCount implements DoctorDailyEventCount{

    private static final List<String> SHMJ = Lists.newArrayList("mnyCount","jxCount","deadCount","blackCount");

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.FARROWING.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto doctorDailyReportDto) {
        DoctorDeliverDailyReport doctorDeliverDailyReport = new DoctorDeliverDailyReport();

        Map<String, Object> extraMap = event.getExtraMap();
        doctorDeliverDailyReport.setNest(doctorDeliverDailyReport.getNest() + 1);
        doctorDeliverDailyReport.setLive(doctorDeliverDailyReport.getLive() + Integer.valueOf(extraMap.get("farrowingLiveCount").toString()));
        doctorDeliverDailyReport.setHealth(doctorDeliverDailyReport.getHealth() + Integer.valueOf(extraMap.get("healthCount").toString()));
        doctorDeliverDailyReport.setWeak(doctorDeliverDailyReport.getWeak() + Integer.valueOf(extraMap.get("weakCount").toString()));
        SHMJ.forEach(s -> doctorDeliverDailyReport.setBlack(doctorDeliverDailyReport.getBlack() + Integer.valueOf(extraMap.get(s).toString())));

        doctorDailyReportDto.getDeliver().addDeliverCount(doctorDeliverDailyReport);
    }
}
