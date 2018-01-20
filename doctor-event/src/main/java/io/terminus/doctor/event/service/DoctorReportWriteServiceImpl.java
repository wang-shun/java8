package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorReportNpdDao;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorReportNpd;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;
    @Autowired
    private DoctorReportNpdDao doctorReportNpdDao;

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;


    @Override
    public void flushNPD(List<Long> farmIds, Date countDate, ReportTime reportTime) {

        if (reportTime == ReportTime.DAY)
            throw new ServiceException("report.time.day.not.support");

        DoctorPigReportReadService.DateDuration dateDuration = doctorPigReportReadService.getDuration(countDate, reportTime);

        flushNPD(farmIds, dateDuration.getStart(), dateDuration.getEnd());
    }

    public void flushNPD(List<Long> farmIds, Date startDate, Date endDate) {


        Map<Long/*farmID*/, Map<Integer/*month*/, Integer/*怀孕天数*/>> farmPregnancy = new ConcurrentHashMap<>();
        Map<Long/*farmID*/, Map<Integer/*month*/, Integer/*哺乳天数*/>> farmLactation = new ConcurrentHashMap<>();
        Map<Long/*farmID*/, Map<Integer/*month*/, Integer/*非生产天数*/>> farmNPD = new ConcurrentHashMap<>();


        Map<String, Object> params = new HashMap<>();
        params.put("farmIds", farmIds);
        params.put("beginDate", startDate);
        params.put("endDate", endDate);
        List<Long> pigs = doctorPigEventDao.findPigAtEvent(startDate, endDate, farmIds);
        pigs.parallelStream().forEach((pigId) -> {

            List<DoctorPigEvent> pigAllEvent = doctorPigEventDao.findByPigId(pigId);

            List<DoctorPigEvent> filterMultiPreCheckEvents = filterMultiPregnancyCheckEvent(pigAllEvent);

            for (int i = 0; i < filterMultiPreCheckEvents.size(); i++) {
                if (i == filterMultiPreCheckEvents.size() - 1)
                    break;

                DoctorPigEvent currentEvent = filterMultiPreCheckEvents.get(i);
                DoctorPigEvent nextEvent = filterMultiPreCheckEvents.get(i + 1);

                if (nextEvent.getType().equals(PigEvent.CHG_FARM.getKey()) || nextEvent.getType().equals(PigEvent.REMOVAL.getKey()))
                    break;


                int days = DateUtil.getDeltaDays(currentEvent.getEventAt(), nextEvent.getEventAt());//天数
                int month = new DateTime(nextEvent.getEventAt()).getMonthOfYear();
                if (nextEvent.getType().equals(PigEvent.FARROWING.getKey())) {
                    if (farmPregnancy.containsKey(nextEvent.getFarmId())) {
                        Map<Integer, Integer> monthPregnancy = farmPregnancy.get(nextEvent.getFarmId());
                        if (monthPregnancy.containsKey(month))
                            monthPregnancy.put(month, monthPregnancy.get(month) + days);
                        else
                            monthPregnancy.put(month, days);
                    } else {
                        Map<Integer, Integer> monthPregnancy = new HashMap<>();
                        monthPregnancy.put(month, days);
                        farmPregnancy.put(nextEvent.getFarmId(), monthPregnancy);
                    }
                } else if (nextEvent.getType().equals(PigEvent.WEAN)) {
                    if (farmLactation.containsKey(nextEvent.getFarmId())) {
                        Map<Integer, Integer> monthLactation = farmLactation.get(nextEvent.getFarmId());
                        if (monthLactation.containsKey(month))
                            monthLactation.put(month, monthLactation.get(month) + days);
                        else
                            monthLactation.put(month, days);
                    } else {
                        Map<Integer, Integer> monthLactation = new HashMap<>();
                        monthLactation.put(month, days);
                        farmLactation.put(nextEvent.getFarmId(), monthLactation);
                    }
                } else {

                    log.debug("猪【{}】的本次事件为【{}】【{}】，下次事件为【{}】【{}】，间隔为【{}】，计入{}月", pigId,
                            PigEvent.from(currentEvent.getType()),
                            DateUtil.toDateString(currentEvent.getEventAt()),
                            PigEvent.from(nextEvent.getType()),
                            DateUtil.toDateString(nextEvent.getEventAt()),
                            days,
                            month);

                    if (farmNPD.containsKey(nextEvent.getFarmId())) {
                        Map<Integer, Integer> monthNPD = farmNPD.get(nextEvent.getFarmId());
                        if (monthNPD.containsKey(month))
                            monthNPD.put(month, monthNPD.get(month) + days);
                        else
                            monthNPD.put(month, days);
                    } else {
                        Map<Integer, Integer> monthNPD = new HashMap<>();
                        monthNPD.put(month, days);
                        farmNPD.put(nextEvent.getFarmId(), monthNPD);
                    }
                }
            }

        });


        int year = new DateTime(startDate).getYear();
        int monthStart = new DateTime(startDate).getMonthOfYear();
        int monthEnd = new DateTime(endDate).getMonthOfYear() + 1;


        farmIds.forEach(f -> {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));

            for (int i = monthStart; i < monthEnd; i++) {

                Date monthStartDate = new DateTime(year, i, 1, 0, 0).toDate();
                Date monthEndDate = DateUtil.monthEnd(monthStartDate);

                int dayCount = DateUtil.getDeltaDays(monthStartDate, monthEndDate) + 1;

                DoctorReportNpd npd = doctorReportNpdDao.findByFarmAndSumAt(f, monthStartDate).orElseGet(() -> new DoctorReportNpd());
                npd.setFarmId(f);
                npd.setDays(dayCount);

//                DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(f, monthStartDate, monthEndDate);
                Integer sowCount = doctorPigDailyDao.countSow(f, monthStartDate, monthEndDate);
                npd.setSowCount(sowCount);
//                if (null != pigDaily) {
//                    int sowCount = (pigDaily.getSowCfEnd() == null ? 0 : pigDaily.getSowCfEnd()) + (pigDaily.getSowPhEnd() == null ? 0 : pigDaily.getSowPhEnd());//母猪月存栏
//                    int sowNotMatingCount = pigDaily.getSowNotMatingCount() == null ? 0 : pigDaily.getSowNotMatingCount();//母猪月进场未配种数
//
//                    npd.setSowCount(sowCount - sowNotMatingCount);
//                } else {
//                    npd.setSowCount(0);
//                }


                npd.setSumAt(monthStartDate);

                if (!farmNPD.containsKey(f))
                    npd.setNpd(0);
                else {
                    Map<Integer, Integer> monthNPD = farmNPD.get(f);
                    if (!monthNPD.containsKey(i))
                        npd.setNpd(0);
                    else
                        npd.setNpd(monthNPD.get(i));
                }
                if (!farmPregnancy.containsKey(f))
                    npd.setPregnancy(0);
                else {
                    Map<Integer, Integer> monthPregnancy = farmPregnancy.get(f);
                    if (!monthPregnancy.containsKey(i))
                        npd.setPregnancy(0);
                    else
                        npd.setPregnancy(monthPregnancy.get(i));
                }
                if (!farmLactation.containsKey(f))
                    npd.setLactation(0);
                else {
                    Map<Integer, Integer> monthLactation = farmLactation.get(f);
                    if (!monthLactation.containsKey(i))
                        npd.setLactation(0);
                    else
                        npd.setLactation(monthLactation.get(i));
                }

                npd.setOrgId(null == farm ? null : farm.getOrgId());
                if (null == npd.getId())
                    doctorReportNpdDao.create(npd);
                else doctorReportNpdDao.update(npd);
            }
        });
    }


    /**
     * 非生产天数最小以猪场作为维度
     *
     * @param pigIds
     */
    @Deprecated
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


    /**
     * 过滤多余的妊娠检查事件
     * 先按照事件发生日期排序，正序
     * 如果是妊娠检查，结果为阳性事件一律去除
     * 如果是妊娠检查，结果为反情，流产，阴性，一个月内保留最后一个妊娠检查事件，其余去除
     * 其他事件类型不影响
     *
     * @return
     */
    private List<DoctorPigEvent> filterMultiPregnancyCheckEvent(List<DoctorPigEvent> pigEvents) {

        List<DoctorPigEvent> sortedByEventDate = pigEvents.stream()
                .sorted((e1, e2) -> e1.getEventAt().compareTo(e2.getEventAt()))
                .collect(Collectors.toList());

        List<DoctorPigEvent> filterMultiPreCheckEvents = new ArrayList<>();
        for (int i = 0; i < sortedByEventDate.size(); i++) { //过滤单月内多余的妊娠检查事件
            if (i == sortedByEventDate.size() - 1) {//最后一笔
                DoctorPigEvent lastEvent = sortedByEventDate.get(i);
                if (lastEvent.getType().equals(PigEvent.PREG_CHECK.getKey()) && lastEvent.getPregCheckResult().equals(PregCheckResult.YANG.getKey())) {
                } else {
                    filterMultiPreCheckEvents.add(sortedByEventDate.get(i));
                }
                break;
            }

            DoctorPigEvent currentEvent = sortedByEventDate.get(i);

            if (currentEvent.getType().equals(PigEvent.PREG_CHECK.getKey())) {

                //如果是阳性，过滤
                if (currentEvent.getPregCheckResult().equals(PregCheckResult.YANG.getKey()))
                    continue;


                boolean remove = false;
                for (int j = i + 1; j < sortedByEventDate.size(); j++) {

                    if (sortedByEventDate.get(j).getType().equals(PigEvent.PREG_CHECK.getKey()))//下一笔还是妊娠检查事件
                        //如果下一笔还是同一个月的
                        if (new DateTime(sortedByEventDate.get(j).getEventAt()).getMonthOfYear() ==
                                new DateTime(sortedByEventDate.get(i).getEventAt()).getMonthOfYear()) {
                            remove = true;
                            continue;//放弃这一笔的妊娠检查事件
                        }
                }
                if (remove)
                    continue;

            }

            filterMultiPreCheckEvents.add(sortedByEventDate.get(i));
        }
        return Collections.unmodifiableList(filterMultiPreCheckEvents);
    }
}
