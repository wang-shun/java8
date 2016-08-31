package io.terminus.doctor.event.report.count;

import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorCheckPregDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 对应妊娠检查
 */
@Component
@Slf4j
public class DoctorDailyPregEventCount implements DoctorDailyEventCount {

    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorDailyPregEventCount(DoctorKpiDao doctorKpiDao) {
        this.doctorKpiDao = doctorKpiDao;
    }


    @Override
    public List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t) {
        return t.stream().filter(e-> Objects.equals(e.getType(), PigEvent.PREG_CHECK.getKey())).collect(Collectors.toList());
    }

    @Override
    public void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto, Map<String, Object> context) {
        //如果事件不为空,全部重新计算
        if (notEmpty(t)) {
            DoctorPigEvent event = t.get(0);
            Date startAt = Dates.startOfDay(event.getEventAt());
            Date endAt = DateUtil.getDateEnd(new DateTime(event.getEventAt())).toDate();

            DoctorCheckPregDailyReport preg = new DoctorCheckPregDailyReport();
            preg.setPositive(doctorKpiDao.checkYangCounts(event.getFarmId(), startAt, endAt));
            preg.setNegative(doctorKpiDao.checkYingCounts(event.getFarmId(), startAt, endAt));
            preg.setLiuchan(doctorKpiDao.checkAbortionCounts(event.getFarmId(), startAt, endAt));
            preg.setFanqing(doctorKpiDao.checkFanQCounts(event.getFarmId(), startAt, endAt));
            doctorDailyReportDto.getCheckPreg().addPregCheckReport(preg);
        }
    }
}
