package io.terminus.doctor.event.cache;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @Autowired
    public DoctorDailyReportCache(DoctorDailyReportReadService doctorDailyReportReadService) {
        this.doctorDailyReportReadService = doctorDailyReportReadService;
    }

    @Getter
    private final Map<String, DoctorDailyReportDto> reportMap = Maps.newHashMap();

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
     * @return
     */
    public DoctorDailyReportDto getDailyReport(Long farmId, Date date) {
        try {
            return reportMap.get(getReportKey(farmId, date));
        } catch (Exception e) {
            log.error("get daily report failed, farmId:{}, date:{}, cause:{}",
                    farmId, date, Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * report put 到缓存
     * @param farmId 猪场id
     * @param date   统计日期
     * @param report 日报统计
     */
    public void putDailyReport(Long farmId, Date date, DoctorDailyReportDto report) {
        synchronized(reportMap) {
            reportMap.put(getReportKey(farmId, date), report);
        }
    }

    private static String getReportKey(Long farmId, Date date) {
        if (farmId == null || date == null) {
            return null;
        }
        return farmId + DateUtil.toDateString(date);
    }
}
