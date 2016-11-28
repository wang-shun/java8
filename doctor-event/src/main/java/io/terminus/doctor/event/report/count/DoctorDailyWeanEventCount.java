package io.terminus.doctor.event.report.count;

import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorWeanDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 断奶的数量统计方式
 */
@Component
public class DoctorDailyWeanEventCount implements DoctorDailyEventCount{

    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorDailyWeanEventCount(DoctorKpiDao doctorKpiDao) {
        this.doctorKpiDao = doctorKpiDao;
    }

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.WEAN.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto doctorDailyReportDto) {
        DoctorWeanDailyReport doctorWeanDailyReport = new DoctorWeanDailyReport();
        Date startAt = Dates.startOfDay(event.getEventAt());
        Date endAt = DateUtil.getDateEnd(new DateTime(event.getEventAt())).toDate();

        doctorWeanDailyReport.setCount(event.getWeanCount());
        doctorWeanDailyReport.setWeight(doctorKpiDao.getWeanPigletWeightAvg(event.getFarmId(), startAt, endAt));
        doctorWeanDailyReport.setNest(1);
        doctorWeanDailyReport.setAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(event.getFarmId(), startAt, endAt));   //断奶日领

        doctorDailyReportDto.getWean().addWeanCount(doctorWeanDailyReport);
    }
}
