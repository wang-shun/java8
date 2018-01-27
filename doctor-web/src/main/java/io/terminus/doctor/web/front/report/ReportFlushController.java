package io.terminus.doctor.web.front.report;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.service.DoctorReportWriteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2018/1/15.
 */
@RestController
@RequestMapping("/api/doctor/report/flush/")
public class ReportFlushController {


    @RpcConsumer
    private DoctorReportWriteService doctorReportWriteService;

    @RequestMapping("npd")
    public void flushNPD(@RequestParam Long farmId,
                         @RequestParam @DateTimeFormat(pattern = "yyyyMM") Date date,
                         @RequestParam(required = false) String type) {

        ReportTime reportTime = ReportTime.MONTH;
        if (StringUtils.isNotBlank(type)) {
            if ("season".equals(type)) {
                reportTime = ReportTime.SEASON;
            } else if ("year".equals(type))
                reportTime = ReportTime.YEAR;
        }
        doctorReportWriteService.flushNPD(Collections.singletonList(farmId), date, reportTime);

    }

    @RequestMapping("all/npd")
    public void flushNPD(@RequestParam Long farmId,
                         @RequestParam @DateTimeFormat(pattern = "yyyyMM") Date date) {

        doctorReportWriteService.flushNPD(Collections.singletonList(farmId), date);
    }

}
