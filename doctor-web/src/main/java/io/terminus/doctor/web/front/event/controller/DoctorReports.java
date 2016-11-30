package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportTrendDto;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryReadService;
import io.terminus.doctor.event.service.DoctorMonthlyReportReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialConsumeProviderReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;

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
     * 分页查询猪群批次总结
     * @return 批次总结
     */
    @RequestMapping(value = "/group/batch/summary", method = RequestMethod.GET)
    public Paging<DoctorGroupBatchSummary> pagingGroupBatchSummary(@RequestParam Map<String, Object> params,
                                                                   @RequestParam(required = false) Integer pageNo,
                                                                   @RequestParam(required = false) Integer pageSize) {
        DoctorGroupSearchDto dto = BeanMapper.map(Params.filterNullOrEmpty(params), DoctorGroupSearchDto.class);
        if (notEmpty(dto.getPigTypeCommas())) {
            dto.setPigTypes(Splitters.splitToInteger(dto.getPigTypeCommas(), Splitters.COMMA));
        }
        Paging<DoctorGroupBatchSummary> paging = RespHelper.or500(doctorGroupBatchSummaryReadService.pagingGroupBatchSummary(dto, pageNo, pageSize));

        //如果猪群没有关闭，刷新下料肉比
        List<DoctorGroupBatchSummary> summaries = paging.getData().stream()
                .map(s -> {
                    if (Objects.equals(s.getStatus(), DoctorGroup.Status.CREATED.getValue())) {
                        Double material = RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, s.getGroupId(), null, null), 0D);
                        s.setFcr(material / s.getFcr());
                    }
                    return s;
                })
                .collect(Collectors.toList());
        return new Paging<>(paging.getTotal(), summaries);
    }
}
