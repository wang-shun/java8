package io.terminus.doctor.event.cache;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigTypeStatisticDao;
import io.terminus.doctor.event.dto.report.daily.DoctorCheckPregDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeadDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDeliverDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorMatingDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorSaleDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorWeanDailyReport;
import io.terminus.doctor.event.service.DoctorDailyGroupReportReadService;
import io.terminus.doctor.event.service.DoctorDailyPigReportReadService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * Desc: 日报统计缓存
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Slf4j
@Component
public class DoctorDailyReportCache {

    private final LoadingCache<String, DoctorDailyReportDto> reportCache;
    private final DoctorDailyPigReportReadService doctorDailyPigReportReadService;
    private final DoctorDailyGroupReportReadService doctorDailyGroupReportReadService;
    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorKpiDao doctorKpiDao;
    private final DoctorPigTypeStatisticDao doctorPigTypeStatisticDao;

    @Autowired
    public DoctorDailyReportCache(DoctorDailyPigReportReadService doctorDailyPigReportReadService,
                                  DoctorDailyGroupReportReadService doctorDailyGroupReportReadService,
                                  DoctorDailyReportDao doctorDailyReportDao,
                                  DoctorKpiDao doctorKpiDao,
                                  DoctorPigTypeStatisticDao doctorPigTypeStatisticDao) {
        this.doctorDailyPigReportReadService = doctorDailyPigReportReadService;
        this.doctorDailyGroupReportReadService = doctorDailyGroupReportReadService;
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorKpiDao = doctorKpiDao;
        this.doctorPigTypeStatisticDao = doctorPigTypeStatisticDao;

        this.reportCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.DAYS).build(new CacheLoader<String, DoctorDailyReportDto>() {
            @Override
            public DoctorDailyReportDto load(String key) throws Exception {
                FarmDate farmDate = parseReportKey(key);
                return farmDate == null ? null : initDailyReportByFarmIdAndDate(farmDate.getFarmId(), farmDate.getDate());
            }
        });
    }

    /**
     * 取出日报缓存
     * @param farmId 猪场id
     * @param date   统计日期
     * @return 日报统计
     */
    public DoctorDailyReportDto getDailyReport(Long farmId, Date date) {
        try {
            return reportCache.get(getReportKey(farmId, date));
        } catch (Exception e) {
            log.error("get daily report failed, farmId:{}, date:{}, cause:{}",
                    farmId, date, Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * report put 到缓存, 覆盖原先的report
     * @param farmId 猪场id
     * @param date   统计日期
     * @param report 日报统计
     */
    public void putDailyReport(Long farmId, Date date, DoctorDailyReportDto report) {
        synchronized(reportCache) {
            reportCache.put(getReportKey(farmId, date), report);
        }
    }

    /**
     * 仅put猪日报, 不修改引用
     * @param reportDto 猪日报
     */
    public void putDailyPigReport(Long farmId, Date date, DoctorDailyReportDto reportDto) {
        synchronized (reportCache) {
            DoctorDailyReportDto report = getDailyReport(farmId,  date);
            if (isNull(report)) {
                putDailyReport(farmId, date, reportDto);
            } else {
                report.setPig(reportDto);
            }
        }
    }

    /**
     * 仅put猪群日报, 不修改引用
     * @param reportDto 猪群日报
     */
    public void putDailyGroupReport(Long farmId, Date date, DoctorDailyReportDto reportDto) {
        synchronized (reportCache) {
            DoctorDailyReportDto report = getDailyReport(farmId,  date);
            if (isNull(report)) {
                putDailyReport(farmId, date, reportDto);
            } else {
                report.setGroup(reportDto);
            }
        }
    }

    /**
     * 清理所有的缓存
     */
    public void clearAllReport() {
        reportCache.invalidateAll();
    }

    //实时Sql查询某猪场的日报统计
    public DoctorDailyReportDto initDailyReportByFarmIdAndDate(Long farmId, Date date) {
        Date startAt = Dates.startOfDay(date);
        Date endAt = DateUtil.getDateEnd(new DateTime(date)).toDate();

        log.info("init daily report farmId:{}", farmId);
        DoctorDailyReportDto report = new DoctorDailyReportDto();

        //妊娠检查
        DoctorCheckPregDailyReport checkPreg = new DoctorCheckPregDailyReport();
        checkPreg.setPositive(doctorKpiDao.checkYangCounts(farmId, startAt, endAt));
        checkPreg.setNegative(doctorKpiDao.checkYingCounts(farmId, startAt, endAt));
        checkPreg.setFanqing(doctorKpiDao.checkFanQCounts(farmId, startAt, endAt));
        checkPreg.setLiuchan(doctorKpiDao.checkAbortionCounts(farmId, startAt, endAt));

        //死淘
        DoctorDeadDailyReport dead = new DoctorDeadDailyReport();
        dead.setBoar(doctorKpiDao.getDeadBoar(farmId, startAt, endAt));
        dead.setSow(doctorKpiDao.getDeadSow(farmId, startAt, endAt));
        dead.setFarrow(doctorKpiDao.getDeadFarrow(farmId, startAt, endAt));
        dead.setNursery(doctorKpiDao.getDeadNursery(farmId, startAt, endAt));
        dead.setFatten(doctorKpiDao.getDeadFatten(farmId, startAt, endAt));

        //分娩
        DoctorDeliverDailyReport deliver = new DoctorDeliverDailyReport();
        deliver.setNest(doctorKpiDao.getDelivery(farmId, startAt, endAt));
        deliver.setLive(doctorKpiDao.getDeliveryAll(farmId, startAt, endAt));
        deliver.setHealth(doctorKpiDao.getDeliveryHealth(farmId, startAt, endAt));
        deliver.setWeak(doctorKpiDao.getDeliveryWeak(farmId, startAt, endAt));
        deliver.setBlack(doctorKpiDao.getDeliveryDeadBlackMuJi(farmId, startAt, endAt));

        //配种
        DoctorMatingDailyReport mating = new DoctorMatingDailyReport();
        mating.setHoubei(doctorKpiDao.firstMatingCounts(farmId, startAt, endAt));
        mating.setPregCheckResultYing(doctorKpiDao.yinMatingCounts(farmId, startAt, endAt));
        mating.setDuannai(doctorKpiDao.weanMatingCounts(farmId, startAt, endAt));
        mating.setFanqing(doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt));
        mating.setLiuchan(doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt));

        //销售
        DoctorSaleDailyReport sale = new DoctorSaleDailyReport();
        sale.setBoar(doctorKpiDao.getSaleBoar(farmId, startAt, endAt));
        sale.setSow(doctorKpiDao.getSaleSow(farmId, startAt, endAt));
        sale.setNursery(doctorKpiDao.getSaleNursery(farmId, startAt, endAt));
        sale.setFatten(doctorKpiDao.getSaleFatten(farmId, startAt, endAt));

        //断奶
        DoctorWeanDailyReport wean = new DoctorWeanDailyReport();
        wean.setCount(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));
        wean.setWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));
        wean.setNest(doctorKpiDao.getWeanSow(farmId, startAt, endAt));

        report.setCheckPreg(checkPreg);
        report.setDead(dead);
        report.setDeliver(deliver);
        report.setMating(mating);
        report.setSale(sale);
        report.setWean(wean);
        return report;
    }

    //实时查询某猪场的日报统计2
    //旧的实时查询, 可能会有错误, 直接使用上一个
    @Deprecated
    private DoctorDailyReportDto initDailyReportByFarmIdAndDateOld(Long farmId, Date date) {
        DoctorDailyReportDto report = new DoctorDailyReportDto();
        report.setPig(RespHelper.orServEx(doctorDailyPigReportReadService.countByFarmIdDate(farmId, date)));
        report.setGroup(RespHelper.orServEx(doctorDailyGroupReportReadService.getGroupDailyReportByFarmIdAndDate(farmId, date)));
        return report;
    }

    //实时查询全部猪场猪和猪群的日报统计
    public List<DoctorDailyReportDto> initDailyReportByDate(Date date) {
        return doctorPigTypeStatisticDao.findAll().stream()
                .map(p -> initDailyReportByFarmIdAndDate(p.getFarmId(), date))
                .collect(Collectors.toList());
    }

    //实时查询全部猪场猪和猪群的日报统计
    @Deprecated
    public List<DoctorDailyReportDto> initDailyReportByDateOld(Date date) {
        Map<Long, DoctorDailyReportDto> pigReportMap = RespHelper.orServEx(doctorDailyPigReportReadService.countByDate(date))
                .stream().collect(Collectors.toMap(DoctorDailyReportDto::getFarmId, v -> v));
        Map<Long, DoctorDailyReportDto> groupReportMap = RespHelper.orServEx(doctorDailyGroupReportReadService.getGroupDailyReportsByDate(date))
                .stream().collect(Collectors.toMap(DoctorDailyReportDto::getFarmId, v -> v));

        //求下 farmIds 的并集
        Set<Long> farmIds = Sets.newHashSet();
        farmIds.addAll(groupReportMap.keySet());
        farmIds.addAll(pigReportMap.keySet());
        Date dateStart = Dates.startOfDay(date);

        //拼接数据
        return farmIds.stream()
                .map(farmId -> {
                    DoctorDailyReportDto report = new DoctorDailyReportDto();
                    report.setFarmId(farmId);
                    report.setSumAt(dateStart);
                    DoctorDailyReportDto pigReport = pigReportMap.get(farmId);
                    if (pigReport != null) {
                        report.setPig(pigReport);
                    }
                    DoctorDailyReportDto groupReport = groupReportMap.get(farmId);
                    if (groupReport != null) {
                        report.setGroup(groupReport);
                    }
                    return report;
                })
                .collect(Collectors.toList());
    }

    private static String getReportKey(Long farmId, Date date) {
        if (farmId == null || date == null) {
            return null;
        }
        return farmId + ":" + DateUtil.toDateString(date);
    }

    private static FarmDate parseReportKey(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }
        List<String> strs = Splitters.COLON.splitToList(key);
        if (strs.size() != 2) {
            return null;
        }

        //这里的时间必须是这一天的最后1秒!
        Date tomorrow = Dates.endOfDay(DateUtil.toDate(strs.get(1)));
        return new FarmDate(Long.valueOf(strs.get(0)), new DateTime(tomorrow).plusSeconds(-1).toDate());
    }

    @Data
    @AllArgsConstructor
    private static class FarmDate {
        private Long farmId;
        private Date date;
    }
}
