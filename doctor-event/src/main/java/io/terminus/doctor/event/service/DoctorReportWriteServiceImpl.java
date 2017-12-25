package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/12/21.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorReportWriteServiceImpl implements DoctorReportWriteService {

    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigReportReadService doctorPigReportReadService;


    @Override
    public void flushNPD(List<Long> farmIds, Date countDate, ReportTime reportTime) {

        DoctorPigReportReadService.DateDuration dateDuration = doctorPigReportReadService.getDuration(countDate, reportTime);

        int sowNumber = 0;
        int npd = 0;

        Map<String, Object> params = new HashMap<>();
        params.put("farmIds", farmIds);
        params.put("type", PigEvent.MATING.getType());
        params.put("beginDate", dateDuration.getStart());
        params.put("endDate", dateDuration.getEnd());

        List<DoctorPigEvent> pigEvents = doctorPigEventDao.list(params);

        Map<Long, DoctorPigEvent> alreadyHandled = new HashMap<>();//会存在同一个猪在这个时间段内的多个配种事件
        for (DoctorPigEvent pigEvent : pigEvents) {

            Map<String, Object> p = new HashMap<>();
            p.put("pigId", pigEvent.getPigId());
            p.put("type", PigEvent.FARROWING.getType());
            List<DoctorPigEvent> fP = doctorPigEventDao.list(p);//进场事件
            if (fP.isEmpty()) {

            } else if (fP.get(0).getEventAt().after(dateDuration.getEnd())) {
                //获取进场时间
                DoctorPigEvent inEvent = doctorPigEventDao.queryLastEventByType(pigEvent.getPigId(), PigEvent.ENTRY.getType());
                if (inEvent.getEventAt().before(dateDuration.getStart())) {
                    npd += DateUtil.getDeltaDays(dateDuration.getStart(), dateDuration.getEnd());
                } else {
                    npd += DateUtil.getDeltaDays(inEvent.getEventAt(), dateDuration.getEnd());
                }

            }
            alreadyHandled.put(pigEvent.getId(), pigEvent);
        }

        //除了配种在时间段内的，还有一种情况是，进场在时间段内，但是第一次配种在时间段外。该猪的所有生命周期都是在非生产中
        params.put("type", PigEvent.ENTRY.getType());
        pigEvents = doctorPigEventDao.list(params);
        for (DoctorPigEvent pigEvent : pigEvents) {
            if (alreadyHandled.containsKey(pigEvent.getId()))
                continue;


        }

//        doctorPigEventDao.countNpdWeanEvent()
    }
}
