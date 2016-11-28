package io.terminus.doctor.event.report.count;

import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorMatingDailyReport;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.MATING.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto doctorDailyReportDto) {
        DoctorMatingDailyReport doctorMatingDailyReport = new DoctorMatingDailyReport();

        log.info("daily mate report event:{},report:{} ", event, doctorDailyReportDto);
        DoctorMatingType matingType = DoctorMatingType.from(event.getDoctorMateType());
        if (matingType == null) {
            return;
        }
        switch (matingType) {
            case HP:
                doctorMatingDailyReport.setHoubei(1);
                break;
            case LPC:
                doctorMatingDailyReport.setLiuchan(1);
                break;
            case LPL:
                doctorMatingDailyReport.setLiuchan(1);
                break;
            case DP:
                doctorMatingDailyReport.setDuannai(1);
                break;
            case YP:
                doctorMatingDailyReport.setPregCheckResultYing(1);
                break;
            case FP:
                doctorMatingDailyReport.setFanqing(1);
                break;
            default:
                return;
        }
        doctorDailyReportDto.getMating().addMatingDaily(doctorMatingDailyReport);
        log.info("daily mate report end event:{}, report:{} ", doctorMatingDailyReport, doctorDailyReportDto);
    }
}
