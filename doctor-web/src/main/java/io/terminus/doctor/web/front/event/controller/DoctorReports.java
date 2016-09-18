package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportTrendDto;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryReadService;
import io.terminus.doctor.event.service.DoctorMonthlyReportReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;

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
    private DoctorDailyReportWriteService doctorDailyReportWriteService;

    @RpcConsumer
    private DoctorMonthlyReportReadService doctorMonthlyReportReadService;

    @RpcConsumer
    private DoctorGroupBatchSummaryReadService doctorGroupBatchSummaryReadService;

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
     * 根据farmId和日期查询猪场日报表(缓存方式)
     * @param farmId  猪场id
     * @param startAt 开始时间 yyyy-MM-dd
     * @param endAt   结束时间 yyyy-MM-dd
     * @return 猪场日报表
     */
    @RequestMapping(value = "/daily/range", method = RequestMethod.GET)
    public List<DoctorDailyReportDto> findDailyReportByFarmIdAndRangeWithCache(@RequestParam("farmId") Long farmId,
                                                                               @RequestParam(value = "startAt", required = false) String startAt,
                                                                               @RequestParam(value = "endAt", required = false) String endAt) {
        return RespHelper.or500(doctorDailyReportReadService.findDailyReportByFarmIdAndRangeWithCache(farmId, startAt, endAt));
    }

    /**
     * 根据farmId和日期查询猪场月报表
     * @param farmId 猪场id
     * @param date   日期 yyyy-MM-dd
     * @return 猪场月报表
     */
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public DoctorMonthlyReportTrendDto findMonthlyReportTrendByFarmIdAndSumAt(@RequestParam("farmId") Long farmId,
                                                                              @RequestParam("date") String date,
                                                                              @RequestParam(value = "index", required = false) Integer index) {
        return RespHelper.or500(doctorMonthlyReportReadService.findMonthlyReportTrendByFarmIdAndSumAt(farmId, date, index));
    }

    /**
     * 清理日报缓存
     * @return 是否成功
     */
    @RequestMapping(value = "/daily/clear", method = RequestMethod.GET)
    public Boolean clearCache() {
        return RespHelper.or500(doctorDailyReportReadService.clearAllReportCache());
    }

    /**
     * 清除某猪场在redis中的日报
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/daily/clearRedis", method = RequestMethod.GET)
    public Boolean clearRedis(@RequestParam("farmId") Long farmId) {
        RespHelper.or500(doctorDailyReportWriteService.deleteDailyReportFromRedis(farmId));
        return true;
    }


    /**
     * 分页查询猪群批次总结
     * @return 批次总结
     */
    @RequestMapping(value = "/group/batch/summary", method = RequestMethod.GET)
    public Paging<DoctorGroupBatchSummary> pagingGroupBatchSummary(@RequestParam Map<String, String> params,
                                                                   @RequestParam(required = false) Integer pageNo,
                                                                   @RequestParam(required = false) Integer pageSize) {
        DoctorGroupSearchDto dto = BeanMapper.map(params, DoctorGroupSearchDto.class);
        if (notEmpty(dto.getPigTypeCommas())) {
            dto.setPigTypes(Splitters.splitToInteger(dto.getPigTypeCommas(), Splitters.COMMA));
        }
        return RespHelper.or500(doctorGroupBatchSummaryReadService.pagingGroupBatchSummary(dto, pageNo, pageSize));
    }
}
