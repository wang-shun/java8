package io.terminus.doctor.event.report.count;

import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorWeanDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 断奶的数量统计方式
 */
@Component
public class DoctorDailyWeanEventCount implements DoctorDailyEventCount{
    @Override
    public List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t) {
        return t.stream().filter(e-> Objects.equals(e.getType(), PigEvent.WEAN.getKey())).collect(Collectors.toList());
    }

    @Override
    public void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto, Map<String, Object> context) {
        DoctorWeanDailyReport doctorWeanDailyReport = new DoctorWeanDailyReport();

        t.forEach(e->{
            Map<String,Object> extraMap = e.getExtraMap();
            doctorWeanDailyReport.setCount(doctorWeanDailyReport.getCount() + Integer.valueOf(extraMap.get("partWeanPigletsCount").toString()));
            doctorWeanDailyReport.setWeight((doctorWeanDailyReport.getWeight() + Double.valueOf(extraMap.get("partWeanAvgWeight").toString()))/2);
        });

        doctorDailyReportDto.getWean().addWeanCount(doctorWeanDailyReport);
    }
}
