package io.terminus.doctor.event.report.count;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.DoctorMatingDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的配种方式事件
 */
@Component
public class DoctorDailyMatingEventCount implements DoctorDailyEventCount {

    private final static ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private final DoctorPigTrackDao doctorPigTrackDao;

    @Autowired
    public DoctorDailyMatingEventCount(DoctorPigTrackDao doctorPigTrackDao){
        this.doctorPigTrackDao = doctorPigTrackDao;
    }

    @Override
    public List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t) {
        return t.stream().filter(e-> Objects.equals(e.getType(), PigEvent.MATING.getKey())).collect(Collectors.toList());
    }

    @Override
    public void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto, Map<String, Object> context) {

        DoctorMatingDailyReport doctorMatingDailyReport = new DoctorMatingDailyReport();

        t.forEach(e->{

            Map<String,Object> extraMap = e.getExtraMap();
            if(extraMap.containsKey("checkResult")){
                PregCheckResult checkResult = PregCheckResult.from(Integer.valueOf(extraMap.get("checkResult").toString()));
                switch (checkResult){
                    case YING:
                        doctorMatingDailyReport.setPregCheckResultYing(doctorMatingDailyReport.getPregCheckResultYing()+1);
                        break;
                    case LIUCHAN:
                        doctorMatingDailyReport.setLiuchan(doctorMatingDailyReport.getLiuchan() + 1);
                        break;
                    case FANQING:
                        doctorMatingDailyReport.setFanqing(doctorMatingDailyReport.getFanqing() + 1);
                        break;
                    default:
                        break;
                }
            }else {
                DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(e.getPigId());
                Map<String,String> result = null;
                try {
                    result = OBJECT_MAPPER.readValue(doctorPigTrack.getRelEventIds(), JacksonType.MAP_OF_STRING);
                } catch (IOException e1) {
                    throw new IllegalStateException("dailyMating.event.fail");
                }
                if(result.size() > 1){
                    doctorMatingDailyReport.setDuannai(doctorMatingDailyReport.getDuannai()+1);
                }else {
                    doctorMatingDailyReport.setHoubei(doctorMatingDailyReport.getHoubei() + 1);
                }
            }
        });

        doctorDailyReportDto.getMating().addMatingDaily(doctorMatingDailyReport);

    }
}
