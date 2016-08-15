package io.terminus.doctor.event.report.count;

import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorMatingDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Slf4j
@Component
public class DoctorDailyMatingEventCount implements DoctorDailyEventCount {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

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
                //Map<String,String> result = null;
                log.info("rel event ids :{} ", doctorPigTrack.getRelEventIds());
                //result = JSON_MAPPER.fromJson(doctorPigTrack.getRelEventIds(), JSON_MAPPER.createCollectionType(Map.class, String.class, String.class));
                if(Splitters.splitToLong(doctorPigTrack.getRelEventIds(), Splitters.COMMA).size() > 1){
                    doctorMatingDailyReport.setDuannai(doctorMatingDailyReport.getDuannai()+1);
                }else {
                    doctorMatingDailyReport.setHoubei(doctorMatingDailyReport.getHoubei() + 1);
                }
            }
        });

        doctorDailyReportDto.getMating().addMatingDaily(doctorMatingDailyReport);

    }
}
