package io.terminus.doctor.event.cache;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
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

    @Autowired
    public DoctorDailyReportCache(DoctorDailyPigReportReadService doctorDailyPigReportReadService,
                                  DoctorDailyGroupReportReadService doctorDailyGroupReportReadService) {
        this.doctorDailyPigReportReadService = doctorDailyPigReportReadService;
        this.doctorDailyGroupReportReadService = doctorDailyGroupReportReadService;

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

    //实时查询某猪场的日报统计
    public DoctorDailyReportDto initDailyReportByFarmIdAndDate(Long farmId, Date date) {
        DoctorDailyReportDto report = new DoctorDailyReportDto();
        report.setPig(RespHelper.orServEx(doctorDailyPigReportReadService.countByFarmIdDate(farmId, date)));
        report.setGroup(RespHelper.orServEx(doctorDailyGroupReportReadService.getGroupDailyReportByFarmIdAndDate(farmId, date)));
        return report;
    }

    //实时查询全部猪场猪和猪群的日报统计
    public List<DoctorDailyReportDto> initDailyReportByDate(Date date) {
        Map<Long, DoctorDailyReportDto> pigReportMap = RespHelper.orServEx(doctorDailyPigReportReadService.countByDate(date))
                .stream().collect(Collectors.toMap(DoctorDailyReportDto::getFarmId, v -> v));
        Map<Long, DoctorDailyReportDto> groupReportMap = RespHelper.orServEx(doctorDailyGroupReportReadService.getGroupDailyReportsByDate(date))
                .stream().collect(Collectors.toMap(DoctorDailyReportDto::getFarmId, v -> v));

        log.info("daily report info: date:{}, pigReport:{}, groupReport:{}", date, pigReportMap, groupReportMap);

        //求下 farmIds 的并集
        Set<Long> farmIds = pigReportMap.keySet();
        farmIds.addAll(groupReportMap.keySet());
        Date dateStart = Dates.startOfDay(date);

        //拼接数据
        return farmIds.stream()
                .map(farmId -> {
                    DoctorDailyReportDto report = new DoctorDailyReportDto();
                    report.setFarmId(farmId);
                    report.setSumAt(dateStart);
                    report.setPig(pigReportMap.get(farmId));
                    report.setGroup(groupReportMap.get(farmId));
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
