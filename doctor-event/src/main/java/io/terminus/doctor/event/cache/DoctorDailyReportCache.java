package io.terminus.doctor.event.cache;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Desc: 日报统计缓存
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Slf4j
@Component
public class DoctorDailyReportCache {

    private final DoctorDailyReportReadService doctorDailyReportReadService;
    private final LoadingCache<String, DoctorDailyReportDto> reportCache;

    @Autowired
    public DoctorDailyReportCache(DoctorDailyReportReadService doctorDailyReportReadService) {
        this.doctorDailyReportReadService = doctorDailyReportReadService;

        this.reportCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.DAYS).build(new CacheLoader<String, DoctorDailyReportDto>() {
            @Override
            public DoctorDailyReportDto load(String key) throws Exception {
                return null;
            }
        });
    }

    @PostConstruct
    public void init() {
        try {
            Date now = new Date();
            List<DoctorDailyReportDto> reportDtos = RespHelper.orServEx(doctorDailyReportReadService.initDailyReportByDate(now));
            reportDtos.forEach(report -> putDailyReport(report.getFarmId(), now, report));
        } catch (ServiceException e) {
            log.error("init daily report failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
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
     * @param report 猪日报
     */
    public void putDailyPigReport(DoctorDailyReportDto report) {
        synchronized (reportCache) {
            getDailyReport(report.getFarmId(), report.getSumAt()).setPig(report);
        }
    }

    /**
     * 仅put猪群日报, 不修改引用
     * @param report 猪群日报
     */
    public void putDailyGroupReport(DoctorDailyReportDto report) {
        synchronized (reportCache) {
            getDailyReport(report.getFarmId(), report.getSumAt()).setGroup(report);
        }
    }

    private static String getReportKey(Long farmId, Date date) {
        if (farmId == null || date == null) {
            return null;
        }
        return farmId + DateUtil.toDateString(date);
    }
}
