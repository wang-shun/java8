package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportDto;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorMonthlyReportReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/report")
public class DoctorReports {

    @RpcConsumer
    private DoctorDailyReportReadService doctorDailyReportReadService;

    @RpcConsumer
    private DoctorMonthlyReportReadService doctorMonthlyReportReadService;

    /**
     * 根据farmId和日期查询猪场日报表(缓存方式)
     * @param farmId 猪场id
     * @param date   日期
     * @return 猪场日报表
     */
    @RequestMapping(value = "/daily", method = RequestMethod.GET)
    public DoctorDailyReportDto findDailyReportByFarmIdAndSumAtWithCache(@RequestParam("farmId") Long farmId,
                                                                         @RequestParam("date") String date) {
        return RespHelper.or500(doctorDailyReportReadService.findDailyReportByFarmIdAndSumAtWithCache(farmId, date));
    }

    /**
     * 根据farmId和日期查询猪场月报表
     * @param farmId 猪场id
     * @param date   日期
     * @return 猪场月报表
     */
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public DoctorMonthlyReportDto findMonthlyReportByFarmIdAndSumAt(@RequestParam("farmId") Long farmId,
                                                                    @RequestParam("date") String date) {
        return RespHelper.or500(doctorMonthlyReportReadService.findMonthlyReportByFarmIdAndSumAt(farmId, date));
    }

    /**
     * 清理日报缓存
     * @return 是否成功
     */
    @RequestMapping(value = "/daily/clear", method = RequestMethod.GET)
    public Boolean clearCache() {
        return RespHelper.or500(doctorDailyReportReadService.clearAllReportCache());
    }
}
