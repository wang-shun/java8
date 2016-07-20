package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
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
public class DoctorDailyReports {

    @RpcConsumer
    private DoctorDailyReportReadService doctorDailyReportReadService;

    /**
     * 根据farmId和日期查询猪场日报表(缓存方式)
     * @param farmId 猪场id
     * @param date   日期
     * @return 猪场日报表
     */
    @RequestMapping(value = "/daily", method = RequestMethod.GET)
    public DoctorDailyReportDto findDailyReportByFarmIdAndSumAtWithCache(@RequestParam("farmId") Long farmId,
                                                                         @RequestParam("date") String date) {
        DoctorLiveStockDailyReport report = new DoctorLiveStockDailyReport();
        report.setFarrow(15);
        report.setFatten(14);
        report.setBoar(5);
        report.setPeihuaiSow(1);
        report.setNursery(90);
        report.setHoubeiSow(10);
        report.setKonghuaiSow(5);
        DoctorDailyReportDto d = new DoctorDailyReportDto();
        d.setLiveStock(report);
        return d;
//        return RespHelper.or500(doctorDailyReportReadService.findDailyReportByFarmIdAndSumAtWithCache(farmId, date));
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
