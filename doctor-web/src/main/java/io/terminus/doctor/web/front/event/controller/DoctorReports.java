package io.terminus.doctor.web.front.event.controller;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorStockStructureDto;
import io.terminus.doctor.event.dto.report.common.DoctorCliqueReportDto;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportTrendDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorRangeReport;
import io.terminus.doctor.event.service.DoctorCommonReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorRangeReportReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorDepartmentReadService;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private DoctorCommonReportReadService doctorCommonReportReadService;

    @RpcConsumer
    private DoctorGroupBatchSummaryReadService doctorGroupBatchSummaryReadService;

    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;

    @RpcConsumer
    private DoctorGroupReadService doctorGroupReadService;

    @RpcConsumer
    private DoctorRangeReportReadService doctorRangeReportReadService;

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RpcConsumer
    private DoctorDepartmentReadService doctorDepartmentReadService;

    /**
     * 根据farmId和日期查询猪场日报表(缓存方式)
     * @param farmId 猪场id
     * @param date   日期 yyyy-MM-dd
     * @return 猪场日报表
     */
    @RequestMapping(value = "/daily", method = RequestMethod.GET)
    public DoctorDailyReportDto findDailyReportByFarmIdAndSumAt(@RequestParam("farmId") Long farmId,
                                                                @RequestParam("date") String date) {
        DoctorDailyReportDto doctorDailyReportDto = new DoctorDailyReportDto();
        if(DateUtil.toDate(date).after(DateUtil.getDateEnd(DateTime.now()).toDate())){
            doctorDailyReportDto.setFail(true);
            return doctorDailyReportDto;
        }
        return RespHelper.or500(doctorDailyReportReadService.findDailyReportDtoByFarmIdAndSumAt(farmId, date));
    }

    /**
     * 查询日报猪场时间段内每天的日报,包含指标
     * @param farmId 猪场id
     * @param startDate   开始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     * @return 猪场日报表
     */
    @RequestMapping(value = "/daily/duration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorDailyReportDto> findDailyReportDtoByFarmIdAndSlot(@RequestParam Long farmId,
                                                                        @RequestParam String startDate,
                                                                        @RequestParam String endDate) {

        if(DateUtil.toDate(endDate).after(new Date())){
            endDate = DateUtil.toDateString(new Date());
        }
        return RespHelper.or500(doctorDailyReportReadService.findDailyReportDtoByFarmIdAndDuration(farmId, startDate, endDate));
    }
    /**
     * 根据farmId和日期查询猪场日报表(缓存方式)
     * @param farmId  猪场id
     * @param startAt 开始时间 yyyy-MM-dd
     * @param endAt   结束时间 yyyy-MM-dd
     * @return 猪场日报表
     */
    @RequestMapping(value = "/daily/range", method = RequestMethod.GET)
    public List<DoctorDailyReport> findDailyReportByFarmIdAndRange(@RequestParam("farmId") Long farmId,
                                                                   @RequestParam(value = "startAt", required = false) String startAt,
                                                                   @RequestParam(value = "endAt", required = false) String endAt) {
        return RespHelper.or500(doctorDailyReportReadService.findDailyReportDtoByFarmIdAndRange(farmId, startAt, endAt));
    }

    /**
     * 根据farmId和日期查询猪场月报表
     * @param farmId 猪场id
     * @param date   日期 yyyy-MM-dd
     * @return 猪场月报表
     */
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public DoctorCommonReportTrendDto findMonthlyReportTrendByFarmIdAndSumAt(@RequestParam("farmId") Long farmId,
                                                                             @RequestParam("date") String date,
                                                                             @RequestParam(value = "index", required = false) Integer index) {
        return RespHelper.or500(doctorCommonReportReadService.findMonthlyReportTrendByFarmIdAndSumAt(farmId, date, index));
    }

    /**
     * 根据farmId和日期段查询猪场月报表
     * @param farmId 猪场id
     * @param startDate 开始日期 yyyy-MM
     * @param endDate 结束日期 yyyy-MM
     * @return 猪场月报表
     */
    @RequestMapping(value = "/monthly/duration", method = RequestMethod.GET)
    public List<DoctorCommonReportTrendDto> findMonthlyReportTrendByFarmIdAndDuration(@RequestParam Long farmId,
                                                                             @RequestParam String startDate,
                                                                             @RequestParam String endDate) {
        return RespHelper.or500(doctorCommonReportReadService.findMonthlyReportTrendByFarmIdAndDuration(farmId, startDate, endDate));
    }


    /**
     * 根据farmId和日期查询猪场月报表
     * @param farmId 猪场id00
     * @param date   日期 yyyy-MM-dd
     * @return 猪场月报表
     */
    @RequestMapping(value = "/monthly/structure", method = RequestMethod.GET)
    public DoctorStockStructureDto findMonthlyReportByFarmIdAndSumAt(@RequestParam("farmId") Long farmId,
                                                                    @RequestParam("date") String date) {
        Response<DoctorRangeReport> reportResponse = doctorRangeReportReadService.findMonthlyByFarmIdAndSumAt(farmId, date);
        if(Arguments.isNull(reportResponse) || Arguments.isNull(reportResponse.getResult())){
            return new DoctorStockStructureDto();
        }
        return reportResponse.getResult().getStockDistributeDto();
    }

    /**
     * 根据farmId和日期查询猪场周报表
     * @param farmId 猪场id
     * @param year   年份  2016
     * @param week   当年第几周 20
     * @return 猪场周报报表
     */
    @RequestMapping(value = "/weekly", method = RequestMethod.GET)
    public DoctorCommonReportTrendDto findWeeklyReportTrendByFarmIdAndSumAt(@RequestParam("farmId") Long farmId,
                                                                            @RequestParam(value = "year", required = false) Integer year,
                                                                            @RequestParam(value = "week", required = false) Integer week,
                                                                            @RequestParam(value = "index", required = false) Integer index) {
        return RespHelper.or500(doctorCommonReportReadService.findWeeklyReportTrendByFarmIdAndSumAt(farmId, year, week, index));
    }

    /**
     * 根据farmId和周段查询猪场周报表
     * @param farmId 猪场id
     * @param year   年份 如2016
     * @param startWeek 开始周
     * @param endWeek   结束周
     * @return 猪场周报报表
     */
    @RequestMapping(value = "/weekly/duration", method = RequestMethod.GET)
    public List<DoctorCommonReportTrendDto> findWeeklyReportTrendByFarmIdAndDuration(@RequestParam Long farmId,
                                                                                     @RequestParam Integer year,
                                                                                     @RequestParam Integer startWeek,
                                                                                     @RequestParam Integer endWeek) {
        return RespHelper.or500(doctorCommonReportReadService.findWeeklyReportTrendByFarmIdAndDuration(farmId, year, startWeek, endWeek));
    }
    /**
     * 分页查询猪群批次总结
     * @return 批次总结
     */
    @RequestMapping(value = "/group/batch/summary", method = RequestMethod.GET)
    public Paging<DoctorGroupBatchSummary> pagingGroupBatchSummary(@RequestParam Map<String, Object> params,
                                                                   @RequestParam(required = false) Integer pageNo,
                                                                   @RequestParam(required = false) Integer pageSize) {
        params = Params.filterNullOrEmpty(params);
        if (!params.containsKey("farmId") || params.get("farmId") == null) {
            return new Paging<>(0L, Collections.emptyList());
        }

        DoctorGroupSearchDto dto = BeanMapper.map(Params.filterNullOrEmpty(params), DoctorGroupSearchDto.class);

        dto.setStartOpenAt(DateUtil.toDate(getDate(params.get("openStartAt"))));
        dto.setEndOpenAt(DateUtil.toDate(getDate(params.get("openEndAt"))));
        dto.setStartCloseAt(DateUtil.toDate(getDate(params.get("closeStartAt"))));
        dto.setEndCloseAt(DateUtil.toDate(getDate(params.get("closeEndAt"))));
        if (params.get("barnId") != null) {
            dto.setCurrentBarnId(Long.valueOf(String.valueOf(params.get("barnId"))));
        }

        if (notEmpty(dto.getPigTypeCommas())) {
            dto.setPigTypes(Splitters.splitToInteger(dto.getPigTypeCommas(), Splitters.COMMA));
        }
        return RespHelper.or500(doctorGroupBatchSummaryReadService.pagingGroupBatchSummary(dto, pageNo, pageSize));
    }

    private static String getDate(Object o) {
        if (o == null) {
            return null;
        }
        return String.valueOf(o);
    }

    /**
     * 查询猪群批次总结
     * @return 批次总结
     */
    @RequestMapping(value = "/group/batch", method = RequestMethod.GET)
    public DoctorGroupBatchSummary getGroupBatchSummary(@RequestParam("groupId") Long groupId,
                                                        @RequestParam(value = "fcc", required = false) Double fcc) {
        DoctorGroupDetail groupDetail = RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId));
        return RespHelper.or500(doctorGroupBatchSummaryReadService.getSummaryByGroupDetail(groupDetail, fcc));
    }

    @RequestMapping(value = "/getBarnLiveStocks", method = RequestMethod.GET)
    public Map<String, Integer> getBarnLiveStocks(@RequestParam Long barnId,
                                           @RequestParam Integer index){
        return RespHelper.or500(doctorCommonReportReadService.findBarnLiveStock(barnId, new Date(), index));
    }

    /**
     * 获取横向报表
     * @param farmIds 猪场id列表
     * @param startDate 开始日期 yyyy-MM-dd
     * @param endDate 结束时间 yyyy-MM-dd
     * @return 横向报表
     */
    @RequestMapping(value = "/transverse/clique", method = RequestMethod.GET)
    public List<DoctorCliqueReportDto> getTransverseCliqueReport(@RequestParam String farmIds,
                                                                 @RequestParam Long farmId,
                                                                 @RequestParam String startDate,
                                                                 @RequestParam String endDate) {
        //获取猪场
        List<Long> farmIdList = Splitters.splitToLong(farmIds, Splitters.UNDERSCORE);
        //获取有权限的猪场id
        List<Long> permissionFarmIds = RespHelper.or500(doctorFarmReadService.findFarmIdsByUserId(UserUtil.getUserId()));

        farmIdList.retainAll(permissionFarmIds);
        if (Arguments.isNullOrEmpty(farmIdList)) {
            return Lists.newArrayList();
        }

        List<DoctorFarm> farmList = RespHelper.or500(doctorFarmReadService.findFarmsByIds(farmIdList));
        Map<Long, String> farmIdToName = farmList.stream()
                .collect(Collectors.toMap(DoctorFarm::getId, DoctorFarm::getName));

        //校验日期(获取月末)
        Date endTime = DateUtil.getMonthEnd(new DateTime(DateUtil.toDate(endDate))).toDate();
        if (endTime.after(new Date())){
            endTime = new Date();
        }
        endDate = DateUtil.toDateString(endTime);
        Long cliqueId = RespHelper.or500(doctorDepartmentReadService.findClique(farmId, Boolean.TRUE)).getId();
        List<DoctorFarm> cliqueFarms = RespHelper.or500(doctorDepartmentReadService.findAllFarmsByOrgId(cliqueId));

        return RespHelper.or500(doctorCommonReportReadService.getTransverseCliqueReport(cliqueId
                , cliqueFarms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), farmIdToName, startDate, endDate));
    }

    /**
     * 获取纵向报表
     * @param farmIds 猪场id列表(可选, 默认拥有权限的猪场)
     * @param startDate 开始日期 yyyy-MM-dd 所在月一号
     * @param endDate 结束时间 yyyy-MM-dd 所在月一号
     * @return 纵向报表
     */
    @RequestMapping(value = "/portrait/clique", method = RequestMethod.GET)
    public List<DoctorCliqueReportDto> getPortraitCliqueReport(@RequestParam(required = false) String farmIds,
                                                               @RequestParam Long farmId,
                                                               @RequestParam String startDate,
                                                               @RequestParam String endDate) {
        List<Long> farmIdList;
        // TODO: 17/6/30 暂时屏蔽前台传值
        farmIds = "";
        if (Strings.isNullOrEmpty(farmIds)) {
            farmIdList = RespHelper.or500(doctorFarmReadService.findFarmIdsByUserId(UserUtil.getUserId()));
        } else {
            farmIdList = Splitters.splitToLong(farmIds, Splitters.UNDERSCORE);
        }
        Long cliqueId = RespHelper.or500(doctorDepartmentReadService.findClique(farmId, Boolean.TRUE)).getId();
        List<DoctorFarm> cliqueFarms = RespHelper.or500(doctorDepartmentReadService.findAllFarmsByOrgId(cliqueId));
        return RespHelper.or500(doctorCommonReportReadService.getPortraitCliqueReport(cliqueId
                , cliqueFarms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), startDate, endDate));
    }
}
