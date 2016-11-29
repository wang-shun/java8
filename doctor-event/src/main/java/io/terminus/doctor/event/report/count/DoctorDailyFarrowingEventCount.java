package io.terminus.doctor.event.report.count;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeliverDailyReport;
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
 * Descirbe: 分娩日报数据统计
 */
@Component
public class DoctorDailyFarrowingEventCount implements DoctorDailyEventCount{

    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorDailyFarrowingEventCount(DoctorKpiDao doctorKpiDao) {
        this.doctorKpiDao = doctorKpiDao;
    }

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.FARROWING.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto doctorDailyReportDto) {
        DoctorDeliverDailyReport report = new DoctorDeliverDailyReport();

        report.setNest(1);
        report.setLive(event.getLiveCount());
        report.setHealth(event.getHealthCount());
        report.setWeak(event.getWeakCount());
        report.setBlack(MoreObjects.firstNonNull(event.getDeadCount(), 0)
                + MoreObjects.firstNonNull(event.getBlackCount(), 0)
                + MoreObjects.firstNonNull(event.getMnyCount(), 0)
                + MoreObjects.firstNonNull(event.getJxCount(), 0));

        Date endAt = DateUtil.getDateEnd(new DateTime(event.getEventAt())).toDate();
        report.setAvgWeight(doctorKpiDao.getFarrowWeightAvg(event.getFarmId(), Dates.startOfDay(event.getEventAt()), endAt));

        doctorDailyReportDto.getDeliver().addDeliverCount(report);
    }
}
