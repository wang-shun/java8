package io.terminus.doctor.move.controller.report;

import com.google.common.base.Stopwatch;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorFarmEarlyEventAtDto;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.service.DoctorDailyReportV2Service;
import io.terminus.doctor.event.service.DoctorReportWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 * 手动刷新报表入口
 */

@Slf4j
@RestController
@RequestMapping("/api/doctor/report")
public class DoctorReportController {

    private final DoctorDailyReportV2Service doctorDailyReportV2Service;
    private final DoctorFarmReadService doctorFarmReadService;

    private final DoctorReportWriteService doctorReportWriteService;


    @Autowired
    public DoctorReportController(DoctorDailyReportV2Service doctorDailyReportV2Service, DoctorFarmReadService doctorFarmReadService, DoctorReportWriteService doctorReportWriteService) {
        this.doctorDailyReportV2Service = doctorDailyReportV2Service;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorReportWriteService = doctorReportWriteService;
    }

    /**
     * @param farmId 猪场id 可选，默认全部猪场
     * @param from   开始时间 yyyy-MM-dd
     * @param to     结束时间，可选，默认当前 yyyy-MM-dd
     * @return 是否成功
     */
    @RequestMapping(value = "/flush/farm/daily", method = RequestMethod.GET)
    public Boolean flushFarmDaily(@RequestParam(required = false) Long farmId,
                                  @RequestParam String from,
                                  @RequestParam(required = false) String to) {
        if (isNull(to)) {
            to = DateUtil.toDateString(new Date());
        }
        if (!isNull(farmId)) {
            return RespHelper.or500(doctorDailyReportV2Service.flushFarmDaily(farmId, from, to));
        }

        String temp = to;
        List<DoctorFarm> farmList = RespHelper.or500(doctorFarmReadService.findAllFarms());
        farmList.parallelStream().forEach(doctorFarm ->
                RespHelper.or500(doctorDailyReportV2Service.flushFarmDaily(doctorFarm.getId(), from, temp)));
        return Boolean.TRUE;
    }

    @RequestMapping(value = "/flush/all/daily")
    public Boolean flushAllDaily() {
        log.info("flush.all.daily.starting");
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<DoctorFarmEarlyEventAtDto> list = RespHelper.or500(doctorDailyReportV2Service.findEarLyAt());
        String end = DateUtil.toDateString(new Date());
        list.forEach(doctorFarmEarlyEventAtDto -> {
            doctorDailyReportV2Service.flushFarmDaily(doctorFarmEarlyEventAtDto.getFarmId(), DateUtil.toDateString(doctorFarmEarlyEventAtDto.getEventAt()), end);
        });
        log.info("flush.all.daily.end, consume:{}m", stopwatch.elapsed(TimeUnit.MINUTES));

        log.info("synchronize.all.daily.starting");
        RespHelper.or500(doctorDailyReportV2Service.synchronizeFullBiData());
        log.info("synchronize.all.daily.end, consume:{}m", stopwatch.elapsed(TimeUnit.MINUTES));

        return Boolean.TRUE;
    }

    @RequestMapping(value = "/flush/daily/after")
    public Boolean flushDailyAfter(@RequestParam String startAt, @RequestParam(required = false) String end) {
        log.info("flush.all.daily.after.starting");
        Date start = DateUtil.toDate(startAt);
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<DoctorFarmEarlyEventAtDto> list = RespHelper.or500(doctorDailyReportV2Service.findEarLyAt());
        if (isNull(end)) {
            end = DateUtil.toDateString(new Date());
        }
        String endAt = end;
        list.forEach(doctorFarmEarlyEventAtDto -> {
            Date begin;
            if (doctorFarmEarlyEventAtDto.getEventAt().before(start)) {
                begin = start;
            } else {
                begin = doctorFarmEarlyEventAtDto.getEventAt();
            }
            doctorDailyReportV2Service.flushFarmDaily(doctorFarmEarlyEventAtDto.getFarmId(), DateUtil.toDateString(begin), endAt);
        });
        log.info("flush.all.daily.end, consume:{}m", stopwatch.elapsed(TimeUnit.MINUTES));

        log.info("synchronize.all.daily.starting");
//        RespHelper.or500(doctorDailyReportV2Service.synchronizeFullBiData());
        log.info("synchronize.all.daily.end, consume:{}m", stopwatch.elapsed(TimeUnit.MINUTES));

        return Boolean.TRUE;
    }

    /**
     * @param farmId 猪场id 可选，默认全部猪场
     * @param from   开始时间 yyyy-MM-dd
     * @param to     结束时间，可选，默认当前 yyyy-MM-dd
     * @return 是否成功
     */
    @RequestMapping(value = "/flush/group/daily", method = RequestMethod.GET)
    public Boolean flushGroupDaily(@RequestParam(required = false) Long farmId,
                                   @RequestParam String from,
                                   @RequestParam(required = false) String to) {
        if (isNull(to)) {
            to = DateUtil.toDateString(new Date());
        }

        if (!isNull(farmId)) {
            return RespHelper.or500(doctorDailyReportV2Service.flushGroupDaily(farmId, from, to));
        }

        String temp = to;
        List<DoctorFarm> farmList = RespHelper.or500(doctorFarmReadService.findAllFarms());
        farmList.stream().parallel().forEach(doctorFarm ->
                RespHelper.or500(doctorDailyReportV2Service.flushGroupDaily(doctorFarm.getId(), from, temp)));
        return Boolean.TRUE;
    }

    /**
     * @param farmId 猪场id 可选，默认全部猪场
     * @param from   开始时间 yyyy-MM-dd
     * @param to     结束时间，可选，默认当前 yyyy-MM-dd
     * @return 是否成功
     */
    @RequestMapping(value = "/flush/pig/daily", method = RequestMethod.GET)
    public Boolean flushPigDaily(@RequestParam(required = false) Long farmId,
                                 @RequestParam String from,
                                 @RequestParam(required = false) String to) {
        if (isNull(to)) {
            to = DateUtil.toDateString(new Date());
        }

        if (!isNull(farmId)) {
            return RespHelper.or500(doctorDailyReportV2Service.flushPigDaily(farmId, from, to));
        }

        String temp = to;
        List<DoctorFarm> farmList = RespHelper.or500(doctorFarmReadService.findAllFarms());
        farmList.stream().parallel().forEach(doctorFarm ->
                RespHelper.or500(doctorDailyReportV2Service.flushPigDaily(doctorFarm.getId(), from, temp)));
        return Boolean.TRUE;
    }

    /**
     * 全量同步报表数据
     *
     * @return
     */
    @RequestMapping(value = "/synchronize/full/bi/data", method = RequestMethod.GET)
    public Boolean synchronizeFullBiData() {
        return RespHelper.or500(doctorDailyReportV2Service.synchronizeFullBiData());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/flush/warehouse")
    public void flushWarehouse(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date date) {
        if (null == date)
            date = new Date();
        doctorDailyReportV2Service.syncWarehouse(date);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/flush/warehouse/all")
    public void flushWarehouseAll(@RequestParam Integer dateType, @RequestParam Integer orgType) {

        if (null == DateDimension.from(dateType))
            throw new ServiceException("date.type.not.support");
        if (null == OrzDimension.from(orgType))
            throw new ServiceException("org.type.not.support");
        doctorDailyReportV2Service.syncWarehouse(dateType, orgType);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/flush/efficiency")
    public void flushEfficiency(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date date) {
        if (null == date)
            date = new Date();
        doctorDailyReportV2Service.syncEfficiency(date);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/flush/efficiency/all")
    public void flushEfficiencyAll(@RequestParam Integer dateType, @RequestParam Integer orgType) {

        if (null == DateDimension.from(dateType))
            throw new ServiceException("date.type.not.support");
        if (null == OrzDimension.from(orgType))
            throw new ServiceException("org.type.not.support");
        doctorDailyReportV2Service.syncEfficiency(dateType, orgType);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/flush/efficiency/all/{farmId}")
    public void flushEfficiencyAll(@PathVariable Long farmId) {
        doctorDailyReportV2Service.syncEfficiency(farmId);
    }

    /**
     * 增量同步
     *
     * @param orzId 猪场id
     * @param start 开始的同步日期 与日报中sumAt比较
     */
    @RequestMapping(value = "/synchronize/delta/bi/data/{orzId}", method = RequestMethod.GET)
    public Boolean synchronizeDeltaDayBiData(@PathVariable Long orzId,
                                             @RequestParam Integer orzType,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start) {
        return RespHelper.or500(doctorDailyReportV2Service.synchronizeDeltaDayBiData(orzId, start, orzType));
    }

    /**
     * 增量同步一个公司以及公司下的所有猪场
     *
     * @param orzId 猪场id
     * @param start 开始的同步日期 与日报中sumAt比较
     */
    @RequestMapping(value = "/synchronize/delta/{orzId}", method = RequestMethod.GET)
    public Boolean synchronizeDelta(@PathVariable Long orzId,
                                    @RequestParam(required = false, defaultValue = "2") Integer orzType,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start) {
        return RespHelper.or500(doctorDailyReportV2Service.synchronizeDelta(orzId, start, orzType));
    }


    /**
     * 增量同步
     *
     * @param start 开始的同步日期 与日报中sumAt比较
     */
    @RequestMapping(value = "/synchronize/all/bi/data", method = RequestMethod.GET)
    public Boolean synchronizeAllDayBiData(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start) {

        List<DoctorFarm> doctorFarms = RespHelper.orServEx(doctorFarmReadService.findAllFarms());
        Set<Long> orzList = doctorFarms.stream().map(DoctorFarm::getId).collect(Collectors.toSet());
        orzList.parallelStream().forEach(orzId ->
                doctorDailyReportV2Service.synchronizeDeltaDayBiData(orzId, start, OrzDimension.FARM.getValue()));

        orzList = doctorFarms.stream().map(DoctorFarm::getOrgId).collect(Collectors.toSet());
        orzList.parallelStream().forEach(orzId ->
                doctorDailyReportV2Service.synchronizeDeltaDayBiData(orzId, start, OrzDimension.ORG.getValue()));
        return Boolean.TRUE;
    }

    @RequestMapping(value = "/yesterday/and/today", method = RequestMethod.GET)
    public Boolean yesterdayAndToday(@RequestParam String date) {
        List<Long> farmList = RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
        doctorDailyReportV2Service.generateYesterdayAndToday(farmList, new DateTime(DateUtil.toDate(date)).withTimeAtStartOfDay().toDate());
        return Boolean.TRUE;
    }

    @RequestMapping(value = "/synchronize/yesterday/and/today", method = RequestMethod.GET)
    public Boolean synchonizeYesterdayAndToday(@RequestParam String date) {
        List<Long> farmList = RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
        doctorDailyReportV2Service.synchronizeYesterdayAndToday(farmList, new DateTime(DateUtil.toDate(date)).withTimeAtStartOfDay().toDate());
        return Boolean.TRUE;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/flush/npd/{farmId:\\d+}")
    public void flushNPD(@PathVariable Long farmId,
                         @RequestParam @DateTimeFormat(pattern = "yyyyMM") Date date,
                         @RequestParam(required = false) String type) {

        ReportTime reportTime = ReportTime.MONTH;
        if (StringUtils.isNotBlank(type)) {
            if ("season".equals(type)) {
                reportTime = ReportTime.SEASON;
            } else if ("year".equals(type))
                reportTime = ReportTime.YEAR;
        }
        //刷新从任意一个时间，到月末，或季末，或年末
        doctorReportWriteService.flushNPD(Collections.singletonList(farmId), date, reportTime);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/flush/npd/{farmId}/all")
    public void flushNPD(@PathVariable Long farmId,
                         @RequestParam @DateTimeFormat(pattern = "yyyyMM") Date date) {

        //刷新从任意一个开始时间到今天。可能横跨多个月，多个季，多个年
        doctorReportWriteService.flushNPD(Collections.singletonList(farmId), date);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/flush/npd/all")
    public void flushNPD(@RequestParam @DateTimeFormat(pattern = "yyyyMM") Date date) {
        doctorReportWriteService.flushNPD(date);
    }


}
