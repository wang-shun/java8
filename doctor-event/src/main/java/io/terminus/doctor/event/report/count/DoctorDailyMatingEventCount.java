package io.terminus.doctor.event.report.count;

import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorMatingDailyReport;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的配种方式事件
 */
@Slf4j
@Component
public class DoctorDailyMatingEventCount implements DoctorDailyEventCount {

    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.MATING.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto doctorDailyReportDto) {
        DoctorMatingDailyReport doctorMatingDailyReport =doctorDailyReportDto.getMating();

        log.info("daily mate report event:{},report:{} ", event, doctorDailyReportDto);
        DoctorMatingType matingType = DoctorMatingType.from(event.getDoctorMateType());
        if (matingType == null) {
            return;
        }
        Date startAt = Dates.startOfDay(event.getEventAt());
        Date endAt = DateUtil.getDateEnd(new DateTime(event.getEventAt())).toDate();

        switch (matingType) {
            case HP:
                doctorMatingDailyReport.setHoubei(doctorKpiDao.firstMatingCounts(event.getFarmId(), startAt, endAt));
                break;
            case LPC: case LPL:
                doctorMatingDailyReport.setLiuchan(doctorKpiDao.abortionMatingCounts(event.getFarmId(), startAt, endAt));
                break;
            case DP:
                doctorMatingDailyReport.setDuannai(doctorKpiDao.weanMatingCounts(event.getFarmId(), startAt, endAt));
                break;
            case YP:
                doctorMatingDailyReport.setPregCheckResultYing(doctorKpiDao.yinMatingCounts(event.getFarmId(), startAt, endAt));
                break;
            case FP:
                doctorMatingDailyReport.setFanqing(doctorKpiDao.fanQMatingCounts(event.getFarmId(), startAt, endAt));
                break;
            default:
                return;
        }
        doctorDailyReportDto.getMating().addMatingDaily(doctorMatingDailyReport);
        log.info("daily mate report end event:{}, report:{} ", doctorMatingDailyReport, doctorDailyReportDto);
    }
}
