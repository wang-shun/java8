package io.terminus.doctor.event.service;

import com.google.common.base.Stopwatch;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.*;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigNpd;
import io.terminus.doctor.event.model.DoctorReportNpd;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private DoctorPigNpdDao doctorPigNpdDao;

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @Override
    public void flushNPD(List<Long> farmIds, Date countDate, ReportTime reportTime) {

        if (reportTime == ReportTime.DAY)
            throw new ServiceException("report.time.day.not.support");

        DoctorPigReportReadService.DateDuration dateDuration = doctorPigReportReadService.getDuration(countDate, reportTime);

        flushNPD(farmIds, dateDuration.getStart(), dateDuration.getEnd());
    }

    @Override
    public void flushNPD(List<Long> farmIds, Date start) {
        Date startAtMonth = DateUtil.monthStart(start);//日期所在月的第一天
        Date end = DateUtil.monthEnd(new Date());

        flushNPD(farmIds, startAtMonth, end);
    }

    @Override
    public void flushNPD(Date start) {

        List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findAllFarms());

        flushNPD(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), start);
    }

    public void flushNPD(List<Long> farmIds, Date startDate, Date endDate) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*怀孕天数*/>> farmPregnancy = new HashMap<>();
        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*哺乳天数*/>> farmLactation = new HashMap<>();
        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*非生产天数*/>> farmNPD = new HashMap<>();
        List<DoctorPigNpd> doctorPigNpds = new ArrayList<>();

        Map<Long, Integer> pigCount = new HashMap<>();
        log.info("start flush npd from {} to {}", startDate, endDate);

        //查询在指定日期内指定猪场发生事件的猪
        //List<Long> pigs = doctorPigEventDao.findPigAtEvent(startDate, endDate, farmIds);

        farmIds.forEach(f -> {

            //查该猪场下所有事件
            Map<Long, List<DoctorPigEvent>> pigEvents = doctorPigEventDao.findForNPD(f, startDate, endDate)
                    .parallelStream()
                    .collect(Collectors.groupingBy(DoctorPigEvent::getPigId));

            log.info("farm {},total {} pig event", f, pigEvents.size());
            //查出所有事件
            pigEvents.forEach((pigId, events) -> {

                DoctorPigNpd pigNpd = new DoctorPigNpd();
                pigNpd.setPigId(pigId);
                pigNpd.setFarmId(f);
                pigNpd.setSumAt(startDate);

                //去除多余的事件
                List<DoctorPigEvent> filterMultiPreCheckEvents = filterMultiPregnancyCheckEvent(events);
                for (int i = 0; i < filterMultiPreCheckEvents.size(); i++) {
//                    if (i == filterMultiPreCheckEvents.size() - 1)
//                        break;

                    DoctorPigEvent currentEvent = filterMultiPreCheckEvents.get(i);
                    DoctorPigEvent beforeEvent = doctorPigEventDao.queryBeforeEvent(currentEvent);
                    if(beforeEvent==null||beforeEvent.getId()==null){
                        continue;
                    }

                    //得到天数
                    int days = DateUtil.getDeltaDays(beforeEvent.getEventAt(), currentEvent.getEventAt());//天数
                    int month = new DateTime(currentEvent.getEventAt()).getMonthOfYear();
                    int year = new DateTime(currentEvent.getEventAt()).getYear();

                    String yearAndMonthKey = year + "-" + month;

                    pigNpd.setOrgId(pigNpd.getOrgId()==null?currentEvent.getOrgId():pigNpd.getOrgId());

                    if (currentEvent.getType().equals(PigEvent.FARROWING.getKey())) {//分娩

                        count(days, currentEvent.getFarmId(), yearAndMonthKey, farmPregnancy);
                        if(currentEvent.getFarmId().equals(f))
                        pigNpd.setPregnancy((pigNpd.getPregnancy()==null?0:pigNpd.getPregnancy())+days);

                    } else if (currentEvent.getType().equals(PigEvent.WEAN.getKey())) {//断奶

                        count(days, currentEvent.getFarmId(), yearAndMonthKey, farmLactation);
                        if(currentEvent.getFarmId().equals(f))
                        pigNpd.setLactation((pigNpd.getLactation()==null?0:pigNpd.getLactation())+days);

                    } else if (currentEvent.getType().equals(PigEvent.CHG_FARM.getKey()) //离场
                            || currentEvent.getType().equals(PigEvent.REMOVAL.getKey())) {
                        if (beforeEvent.getType().equals(PigEvent.FARROWING.getKey())) {
                            //离场前分娩，算哺乳时间
                            count(days, currentEvent.getFarmId(), yearAndMonthKey, farmLactation);
                            if(currentEvent.getFarmId().equals(f))
                            pigNpd.setLactation((pigNpd.getLactation()==null?0:pigNpd.getLactation())+days);

                        }else if(beforeEvent.getType().equals(PigEvent.MATING.getKey())
                                &&(currentEvent.getPigStatusBefore()==PigStatus.Pregnancy.getKey()||currentEvent.getPigStatusBefore()==PigStatus.Farrow.getKey())){
                            //离场前配种，算孕期
                            count(days, currentEvent.getFarmId(), yearAndMonthKey, farmPregnancy);
                            if(currentEvent.getFarmId().equals(f))
                            pigNpd.setPregnancy((pigNpd.getPregnancy()==null?0:pigNpd.getPregnancy())+days);

                        } else /*if (currentEvent.getType().equals(PigEvent.ENTRY.getKey())
                                || currentEvent.getType().equals(PigEvent.WEAN.getKey())
                                || currentEvent.getType().equals(PigEvent.PREG_CHECK.getKey())
                                || currentEvent.getType().equals(PigEvent.MATING.getKey()))*/ {

                            pigCount.compute(pigId, (k, v) -> null == v ? 1 : v + 1);
                            count(days, currentEvent.getFarmId(), yearAndMonthKey, farmNPD);
                            if(currentEvent.getFarmId().equals(f))
                            pigNpd.setNpd((pigNpd.getNpd()==null?0:pigNpd.getNpd())+days);

                        }

                    } else {

                        pigCount.compute(pigId, (k, v) -> null == v ? 1 : v + 1);
                        count(days, currentEvent.getFarmId(), yearAndMonthKey, farmNPD);
                        if(currentEvent.getFarmId().equals(f))
                        pigNpd.setNpd((pigNpd.getNpd()==null?0:pigNpd.getNpd())+days);
                    }
                }
                doctorPigNpds.add(pigNpd);

            });
        });

        farmIds.forEach(f -> {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));

            for (Date i = startDate; i.before(endDate); i = DateUtils.addMonths(i, 1)) {

                Date monthEndDate = DateUtil.monthEnd(i);

                int dayCount = DateUtil.getDeltaDays(i, monthEndDate) + 1;

                DoctorReportNpd npd = doctorReportNpdDao.findByFarmAndSumAt(f, i).orElseGet(() -> new DoctorReportNpd());
                npd.setFarmId(f);
                npd.setDays(dayCount);

                if (log.isDebugEnabled())
                    log.debug("刷新猪场{},从{}到{}", farm.getId(), DateUtil.toDateString(i), DateUtil.toDateString(monthEndDate));
                Integer sowCount = doctorPigDailyDao.countSow(f, i, monthEndDate);
                npd.setSowCount(sowCount);

                npd.setSumAt(i);

                int year = new DateTime(i).getYear();
                int month = new DateTime(i).getMonthOfYear();
                String monthAndYearKey = year + "-" + month;

                npd.setNpd(getCount(f, monthAndYearKey, farmNPD));
                npd.setPregnancy(getCount(f, monthAndYearKey, farmPregnancy));
                npd.setLactation(getCount(f, monthAndYearKey, farmLactation));

                npd.setOrgId(null == farm ? null : farm.getOrgId());
                if (null == npd.getId())
                    doctorReportNpdDao.create(npd);
                else
                    doctorReportNpdDao.update(npd);
            }
        });

        try {
            doctorPigNpdDao.creates(doctorPigNpds);
        }catch (Exception e){
            log.error("DoctorReportWriteServiceImpl.createsPigNpd:"+e.getMessage());
        }
        log.info("total {} pig", pigCount.size());
        log.debug("use {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }


    /**
     * 过滤多余的事件
     * 先按照事件发生日期排序，正序
     * 如果是妊娠检查，结果为阳性事件一律去除
     * 如果是妊娠检查，结果为反情，流产，阴性，一个月内保留最后一个妊娠检查事件，其余去除
     * 如果是配种事件，并且是复配，过滤
     * 其他事件类型不影响
     *
     * @param pigEvents 已经过滤掉了防疫、疾病、转舍、体况
     * @return
     */
    public List<DoctorPigEvent> filterMultiPregnancyCheckEvent(List<DoctorPigEvent> pigEvents) {

        List<DoctorPigEvent> sortedByEventDate = pigEvents.stream()
                .sorted((e1, e2) -> e1.getEventAt().compareTo(e2.getEventAt()))
                .collect(Collectors.toList());

        List<DoctorPigEvent> filterMultiPreCheckEvents = new ArrayList<>();

        for (int i = 0; i < sortedByEventDate.size(); i++) {

            DoctorPigEvent currentEvent = sortedByEventDate.get(i);

            if (currentEvent.getType().equals(PigEvent.MATING.getKey())) {
                if (null == currentEvent.getCurrentMatingCount())
                    log.warn("current mating count missing,unknown mating several times.pig event[{}]", currentEvent.getId());
                else if (currentEvent.getCurrentMatingCount() > 1)
                    //复配不参与计算NPD
                    continue;
            }

            if (currentEvent.getType().equals(PigEvent.PREG_CHECK.getKey())) {

                if (currentEvent.getPregCheckResult() == null) {
                    log.warn("event[{}] is pregnancy check and has no check result", currentEvent.getId());
                    continue;
                }

                //只留一次妊娠检查
                if (i != sortedByEventDate.size() - 1) {
                    boolean remove = false;
                    for (int j = i + 1; j < sortedByEventDate.size(); j++) {
                        if (sortedByEventDate.get(j).getType().equals(PigEvent.PREG_CHECK.getKey()))//下一笔还是妊娠检查事件
                            //如果下一笔还是同一个月的
                            if (DateUtils.isSameDay(sortedByEventDate.get(j).getEventAt(), currentEvent.getEventAt())) {
                                remove = true;
                                break;//放弃这一笔的妊娠检查事件
                            }
                    }
                    if (remove)
                        continue;
                }


                //如果是阳性，过滤
                if (currentEvent.getPregCheckResult().equals(PregCheckResult.YANG.getKey())) {
                    continue;
                }

            }

            filterMultiPreCheckEvents.add(currentEvent);
        }
        return Collections.unmodifiableList(filterMultiPreCheckEvents);
    }

    private void count(int days,
                       Long farmId,
                       String yearAndMonth,
                       Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*天数*/>> counter) {

        if (counter.containsKey(farmId)) {
            Map<String, Integer> monthCount = counter.get(farmId);
            if (monthCount.containsKey(yearAndMonth)) {
                int oldValue = monthCount.get(yearAndMonth);
                int newValue = days;
                int nowValue = oldValue + newValue;
//                log.info("{}-{}，历史{},新{},计算后{}", farmId, yearAndMonth, oldValue, newValue, nowValue);
                monthCount.put(yearAndMonth, nowValue);
            } else
                monthCount.put(yearAndMonth, days);
        } else {
            Map<String, Integer> monthCount = new HashMap<>();
            monthCount.put(yearAndMonth, days);
            counter.put(farmId, monthCount);
        }
    }

    private int getCount(Long farmId, String yearAndMonth,
                         Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*天数*/>> counter) {
        if (!counter.containsKey(farmId))
            return 0;
        else {
            Map<String, Integer> monthCount = counter.get(farmId);
            if (!monthCount.containsKey(yearAndMonth))
                return 0;
            else
                return monthCount.get(yearAndMonth);
        }
    }
}
