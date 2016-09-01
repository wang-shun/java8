package io.terminus.doctor.event.report.count;

import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorWeanDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 断奶的数量统计方式
 */
@Component
public class DoctorDailyWeanEventCount implements DoctorDailyEventCount{

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.WEAN.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto doctorDailyReportDto) {
        DoctorWeanDailyReport doctorWeanDailyReport = new DoctorWeanDailyReport();

        Map<String,Object> extraMap = event.getExtraMap();
        doctorWeanDailyReport.setCount(doctorWeanDailyReport.getCount() + Integer.valueOf(extraMap.get("partWeanPigletsCount").toString()));
        doctorWeanDailyReport.setWeight((doctorWeanDailyReport.getWeight() + Double.valueOf(extraMap.get("partWeanAvgWeight").toString()))/2);
        doctorWeanDailyReport.setNest(doctorWeanDailyReport.getNest() + 1);

        doctorDailyReportDto.getWean().addWeanCount(doctorWeanDailyReport);
    }
}
