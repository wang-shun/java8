package io.terminus.doctor.web.front.report;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.service.DoctorDailyReportV2ReadService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * author:xjn
 * email:xiaojiannan@terminus.io
 * date:18/5/14
 */
@Api("APP端报表")
@RestController
@RequestMapping("/api/doctor/app")
public class AppReportController {

    @RpcConsumer
    private DoctorDailyReportV2ReadService doctorDailyReportV2ReadService;

    private final ReportBoardHelper helper;

    @Autowired
    public AppReportController(ReportBoardHelper helper) {
        this.helper = helper;
    }

    @ApiOperation("查询报表详情报表")
    @RequestMapping(value = "/query/report/detail", method = RequestMethod.GET)
    public List<Map<String, String>> queryReportDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {

        Date tomorrow = DateTime.now().plusDays(1).withTimeAtStartOfDay().toDate();

        if (notNull(dimensionCriteria.getStartAt()) && !dimensionCriteria.getStartAt().before(tomorrow)) {
            throw new JsonResponseException("start.at.error");
        }
        if (notNull(dimensionCriteria.getEndAt()) && !dimensionCriteria.getEndAt().before(tomorrow)) {
            throw new JsonResponseException("end.at.error");
        }
        return helper.fillRegionReport(dimensionCriteria);
    }
}
