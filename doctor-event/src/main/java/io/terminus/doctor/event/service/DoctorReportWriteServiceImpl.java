package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    private DoctorPigDailyDao doctorPigDailyDao;


    @Override
    public void flushNPD(List<Long> farmIds, Date countDate, ReportTime reportTime) {
        DoctorPigReportReadService.DateDuration dateDuration = doctorPigReportReadService.getDuration(countDate, reportTime);
        flushNPD(farmIds, dateDuration.getStart(), dateDuration.getEnd());
    }

    @Override
    public void flushNPD(List<Long> farmIds, Date startDate, Date endDate) {


        Map<Long/*farmID*/, Integer> farmPD = new HashMap<>();
        Map<Long/*farmID*/, Integer> farmNPD = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("farmIds", farmIds);
        params.put("beginDate", startDate);
        params.put("endDate", endDate);
        doctorPigEventDao.list(params)
                .stream()
                .collect(Collectors.groupingBy(DoctorPigEvent::getPigId))
                .forEach((pigId, events) -> {

                    List<DoctorPigEvent> sortedByEventDate = events.stream().sorted((e1, e2) -> e1.getEventAt().compareTo(e2.getEventAt())).collect(Collectors.toList());

                    List<DoctorPigEvent> filterMultiPreCheckEvents = new ArrayList<>();
                    for (int i = 0; i < sortedByEventDate.size(); i++) {
                        if (i == events.size() - 1) {//最后一笔
                            filterMultiPreCheckEvents.add(events.get(i));
                            break;
                        }
                        DoctorPigEvent nextEvent = events.get(i + 1);
                        if (nextEvent.getType().equals(PigEvent.PREG_CHECK.getKey())) {//下一笔还是妊娠检查事件
                            //如果下一笔还是同一个月的
                            if (new DateTime(nextEvent.getEventAt()).getMonthOfYear() ==
                                    new DateTime(events.get(i).getEventAt()).getMonthOfYear())
                                continue;//放弃这一笔的妊娠检查事件
                        }

                        filterMultiPreCheckEvents.add(events.get(i));
                    }

                    for (int i = 0; i < filterMultiPreCheckEvents.size(); i++) {
                        if (i == filterMultiPreCheckEvents.size() - 1)
                            break;

                        DoctorPigEvent currentEvent = filterMultiPreCheckEvents.get(i);
                        DoctorPigEvent nextEvent = filterMultiPreCheckEvents.get(i + 1);

                        if (nextEvent.getType().equals(PigEvent.FARROWING.getKey()) || nextEvent.getType().equals(PigEvent.WEAN)) {
                            if (farmPD.containsKey(nextEvent.getFarmId()))
                                farmPD.put(nextEvent.getFarmId(), farmPD.get(nextEvent.getEventAt()) + DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));
                            else
                                farmPD.put(nextEvent.getFarmId(), DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));
                        } else {
                            if (farmNPD.containsKey(nextEvent.getFarmId()))
                                farmNPD.put(nextEvent.getFarmId(), farmNPD.get(nextEvent.getEventAt()) + DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));
                            else
                                farmNPD.put(nextEvent.getFarmId(), DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));

                            if (nextEvent.getType().equals(PigEvent.CHG_FARM.getKey()) || nextEvent.getType().equals(PigEvent.REMOVAL.getKey()))
                                break;
                        }

                    }

                });


        farmIds.forEach(f -> {
            DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(f, startDate, endDate);
            int sowCount = pigDaily.getSowCfEnd() + pigDaily.getSowPhEnd();
            int sowNotMatingCount = pigDaily.getSowNotMatingCount();
            int dayCount = DateUtil.getDeltaDays(startDate, endDate);
//             sowCount - sowNotMatingCount / dayCount;
        });

    }


    /**
     * 非生产天数最小以猪场作为维度
     *
     * @param pigIds
     */
    @Override
    public void flushNPD(List<Long> pigIds) {

        Map<Date, Integer> monthlyNPD = new HashMap<>();
        Map<Date, Integer> monthlyPD = new HashMap<>();

        pigIds.forEach(p -> {
            List<DoctorPigEvent> events = doctorPigEventDao.queryAllEventsByPigIdForASC(p);

            //过滤多余的妊娠检查事件
            List<DoctorPigEvent> filterMultiPreCheckEvents = new ArrayList<>();
            for (int i = 0; i < events.size(); i++) {
                if (i == events.size() - 1) {//最后一笔
                    filterMultiPreCheckEvents.add(events.get(i));
                    break;
                }

                DoctorPigEvent nextEvent = events.get(i + 1);
                if (nextEvent.getType().equals(PigEvent.PREG_CHECK.getKey())) {//下一笔还是妊娠检查事件
                    //如果下一笔还是同一个月的
                    if (new DateTime(nextEvent.getEventAt()).getMonthOfYear() ==
                            new DateTime(events.get(i).getEventAt()).getMonthOfYear())
                        continue;//放弃这一笔的妊娠检查事件
                }

                filterMultiPreCheckEvents.add(events.get(i));
            }

            for (int i = 0; i < filterMultiPreCheckEvents.size(); i++) {
                if (i == filterMultiPreCheckEvents.size() - 1)
                    break;

                DoctorPigEvent currentEvent = filterMultiPreCheckEvents.get(i);
                DoctorPigEvent nextEvent = filterMultiPreCheckEvents.get(i + 1);

                if (nextEvent.getType().equals(PigEvent.FARROWING.getKey()) || nextEvent.getType().equals(PigEvent.WEAN)) {
                    if (monthlyPD.containsKey(nextEvent.getEventAt()))
                        monthlyPD.put(nextEvent.getEventAt(), monthlyNPD.get(nextEvent.getEventAt()) + DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));
                    else
                        monthlyPD.put(nextEvent.getEventAt(), DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));
                } else {
                    if (monthlyNPD.containsKey(nextEvent.getEventAt()))
                        monthlyNPD.put(nextEvent.getEventAt(), monthlyNPD.get(nextEvent.getEventAt()) + DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));
                    else
                        monthlyNPD.put(nextEvent.getEventAt(), DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt()));

                    if (nextEvent.getType().equals(PigEvent.CHG_FARM.getKey()) || nextEvent.getType().equals(PigEvent.REMOVAL.getKey()))
                        break;
                }

            }
        });

        //

    }

    @Override
    public void flushBirthCount() {

    }
}
