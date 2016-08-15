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
     * @param date   日期 yyyy-MM-dd
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
     * @param date   日期 yyyy-MM-dd
     * @return 猪场月报表
     */
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public DoctorMonthlyReportDto findMonthlyReportByFarmIdAndSumAt(@RequestParam("farmId") Long farmId,
                                                                    @RequestParam("date") String date) {
//        DoctorMonthlyReportDto report = new DoctorMonthlyReportDto();
//        report.setMateHoubei(RandomUtil.random(1, 100));
//        report.setMateWean(RandomUtil.random(1, 100));
//        report.setMateFanqing(RandomUtil.random(1, 100));
//        report.setMateAbort(RandomUtil.random(1, 100));
//        report.setMateNegtive(RandomUtil.random(1, 100));
//        report.setMateEstimatePregRate(RandomUtil.random(1, 100));
//        report.setMateRealPregRate(RandomUtil.random(1, 100));
//        report.setMateEstimateFarrowingRate(RandomUtil.random(1, 100));
//        report.setMateRealFarrowingRate(RandomUtil.random(1, 100));
//        report.setCheckPositive(RandomUtil.random(1, 100));
//        report.setCheckFanqing(RandomUtil.random(1, 100));
//        report.setCheckAbort(RandomUtil.random(1, 100));
//        report.setCheckNegtive(RandomUtil.random(1, 100));
//        report.setFarrowEstimateParity(RandomUtil.random(1, 100));
//        report.setFarrowNest(RandomUtil.random(1, 100));
//        report.setFarrowAlive(RandomUtil.random(1, 100));
//        report.setFarrowHealth(RandomUtil.random(1, 100));
//        report.setFarrowWeak(RandomUtil.random(1, 100));
//        report.setFarrowDead(RandomUtil.random(1, 100));
//        report.setFarrowMny(RandomUtil.random(1, 100));
//        report.setFarrowAll(RandomUtil.random(1, 100));
//        report.setFarrowAvgHealth(RandomUtil.random(1, 100));
//        report.setFarrowAvgAll(RandomUtil.random(1, 100));
//        report.setFarrowAvgAlive(RandomUtil.random(1, 100));
//        report.setWeanSow(RandomUtil.random(1, 100));
//        report.setWeanPiglet(RandomUtil.random(1, 100));
//        report.setWeanAvgWeight(RandomUtil.random(1, 100));
//        report.setWeanAvgCount(RandomUtil.random(1, 100));
//        report.setSaleSow(RandomUtil.random(1, 100));
//        report.setSaleBoar(RandomUtil.random(1, 100));
//        report.setSaleNursery(RandomUtil.random(1, 100));
//        report.setSaleFatten(RandomUtil.random(1, 100));
//        report.setDeadSow(RandomUtil.random(1, 100));
//        report.setDeadBoar(RandomUtil.random(1, 100));
//        report.setDeadFarrow(RandomUtil.random(1, 100));
//        report.setDeadNursery(RandomUtil.random(1, 100));
//        report.setDeadFatten(RandomUtil.random(1, 100));
//        report.setDeadFarrowRate(RandomUtil.random(1, 100));
//        report.setDeadNurseryRate(RandomUtil.random(1, 100));
//        report.setDeadFattenRate(RandomUtil.random(1, 100));
//        report.setNpd(RandomUtil.random(1, 100));
//        report.setPsy(RandomUtil.random(1, 100));
//        return report;
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
