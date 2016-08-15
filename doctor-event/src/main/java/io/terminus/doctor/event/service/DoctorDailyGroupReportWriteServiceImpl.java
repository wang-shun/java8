package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */
@Slf4j
@Service
public class DoctorDailyGroupReportWriteServiceImpl implements DoctorDailyGroupReportWriteService {

    private final DoctorDailyGroupReportReadService doctorDailyGroupReportReadService;
    private final DoctorDailyReportCache doctorDailyReportCache;

    @Autowired
    public DoctorDailyGroupReportWriteServiceImpl(DoctorDailyGroupReportReadService doctorDailyGroupReportReadService,
                                                  DoctorDailyReportCache doctorDailyReportCache) {
        this.doctorDailyGroupReportReadService = doctorDailyGroupReportReadService;
        this.doctorDailyReportCache = doctorDailyReportCache;
    }

    @Override
    public Response<Boolean> updateDailyGroupReportCache(Long eventId) {
        try {
            DoctorDailyReportDto report = RespHelper.orServEx(doctorDailyGroupReportReadService.getGroupDailyReportByEventId(eventId));
            doctorDailyReportCache.putDailyGroupReport(report.getFarmId(), report.getSumAt(), report);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update daily group report cache failed, eventId:{}, cause:{}",
                    eventId, Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }
    }
}
