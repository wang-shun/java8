package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */
@Slf4j
@Service
public class DoctorDailyGroupReportReadServiceImpl implements DoctorDailyGroupReportReadService {

    private final DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;
    private final DoctorGroupReadService doctorGroupReadService;

    @Autowired
    public DoctorDailyGroupReportReadServiceImpl(DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService,
                                                 DoctorGroupReadService doctorGroupReadService) {
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public Response<DoctorDailyReportDto> getGroupDailyReportByFarmIdAndDate(Long farmId, Date date) {
        try {
            DoctorDailyReportDto report = new DoctorDailyReportDto();

            return Response.ok(report);
        } catch (Exception e) {
            log.error("get group daily report by farmId and date failed, farmId:{}, date:{}, cause:{}",
                    farmId, date, Throwables.getStackTraceAsString(e));
            return Response.fail("get.group.daily.report.fail");
        }
    }

    @Override
    public Response<List<DoctorDailyReportDto>> getGroupDailyReportsByDate(Date date) {
        try {

            return Response.ok();
        } catch (Exception e) {
            log.error("get group daily report by farmId and date failed, date:{}, cause:{}",
                    date, Throwables.getStackTraceAsString(e));
            return Response.fail("get.group.daily.report.fail");
        }
    }
}
