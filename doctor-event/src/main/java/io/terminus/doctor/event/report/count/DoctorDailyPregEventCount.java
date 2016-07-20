package io.terminus.doctor.event.report.count;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dto.report.DoctorCheckPregDailyReport;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.doctor.event.enums.PregCheckResult.YANG;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 对应妊娠检查
 */
@Component
@Slf4j
public class DoctorDailyPregEventCount implements DoctorDailyEventCount {


    @Override
    public List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t) {
        return t.stream().filter(e-> Objects.equals(e.getType(), PigEvent.PREG_CHECK.getKey())).collect(Collectors.toList());
    }

    @Override
    public void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto, Map<String, Object> context) {

        DoctorCheckPregDailyReport doctorCheckPregDailyReport = new DoctorCheckPregDailyReport();

        // count daily preg check count
        t.stream().forEach(e->{

            Map<String,Object> extraMap = e.getExtraMap();
            PregCheckResult pregCheckResult = PregCheckResult.from(Integer.valueOf(extraMap.get("checkResult").toString()));

            switch (pregCheckResult){
                case YANG:
                    doctorCheckPregDailyReport.setPositive(doctorCheckPregDailyReport.getPositive() + 1);
                    break;
                case YING:
                    doctorCheckPregDailyReport.setNegative(doctorCheckPregDailyReport.getNegative() + 1);
                    break;
                case LIUCHAN:
                    doctorCheckPregDailyReport.setLiuchan(doctorCheckPregDailyReport.getLiuchan() + 1);
                    break;
                case FANQING:
                    doctorCheckPregDailyReport.setFanqing(doctorCheckPregDailyReport.getFanqing() + 1);
                    break;
                default:
                    break;
            }
        });

        doctorDailyReportDto.getCheckPreg().addPregCheckReport(doctorCheckPregDailyReport);

    }
}
