package io.terminus.doctor.web.front.report;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesReadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/1/3.
 */
@Api("猪场看板报表")
@RestController
@RequestMapping("/api/doctor/report/board/{farmId}/")
public class ReportBoardController {

    @RpcConsumer
    private DoctorReportFieldCustomizesReadService doctorReportFieldCustomizesReadService;

    @ApiOperation("看板日报表")
    @RequestMapping(method = RequestMethod.GET, value = "daily")
    public List<DoctorReportFieldTypeDto> dailyBoard(@ApiParam("猪场名称")
                                                     @PathVariable Long farmId,
                                                     @ApiParam("查询日期")
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        Response<List<DoctorReportFieldTypeDto>> result = doctorReportFieldCustomizesReadService.getAllWithSelected(farmId);

        return null;
    }


    @ApiOperation("看板周报表")
    @RequestMapping(method = RequestMethod.GET, value = "weekly")
    public List<DoctorReportFieldTypeDto> weeklyBoard(@ApiParam("猪场名称")
                                                      @PathVariable Long farmId,
                                                      @ApiParam("查询日期， 本周第一天")
                                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return null;
    }

    @ApiOperation("看板月报表")
    @RequestMapping(method = RequestMethod.GET, value = "monthly")
    public List<DoctorReportFieldTypeDto> monthlyBoard(@ApiParam("猪场名称")
                                                       @PathVariable Long farmId,
                                                       @ApiParam("查询日期， 本月第一天")
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return null;
    }

    @ApiOperation("看板季报表")
    @RequestMapping(method = RequestMethod.GET, value = "quarterly")
    public List<DoctorReportFieldTypeDto> quarterlyBoard(@ApiParam("猪场名称")
                                                         @PathVariable Long farmId,
                                                         @ApiParam("查询日期， 本季第一天")
                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return null;
    }

    @ApiOperation("看板年报表")
    @RequestMapping(method = RequestMethod.GET, value = "yearly")
    public List<DoctorReportFieldTypeDto> yearlyBoard(@ApiParam("猪场名称")
                                                      @PathVariable Long farmId,
                                                      @ApiParam("查询日期， 本年第一天")
                                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return null;
    }

}
