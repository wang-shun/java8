package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.*;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportDto;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportTrendDto;
import io.terminus.doctor.event.dto.report.common.DoctorGroupLiveStockDetailDto;
import io.terminus.doctor.event.enums.ReportRangeType;
import io.terminus.doctor.event.model.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 猪场报表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Slf4j
@Service
@RpcProvider
public class DoctorCommonReportReadServiceImpl implements DoctorCommonReportReadService {

    private static final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.nonEmptyMapper();
    private static final int MONTH_INDEX = 12;
    private static final int WEEK_INDEX = 20;

    private final DoctorParityMonthlyReportDao doctorParityMonthlyReportDao;
    private final DoctorBoarMonthlyReportDao doctorBoarMonthlyReportDao;
    private final DoctorKpiDao doctorKpiDao;
    private final DoctorRangeReportDao doctorRangeReportDao;
    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorDailyGroupDao doctorDailyGroupDao;

    @Autowired
    public DoctorCommonReportReadServiceImpl(
                                             DoctorParityMonthlyReportDao doctorParityMonthlyReportDao,
                                             DoctorBoarMonthlyReportDao doctorBoarMonthlyReportDao,
                                             DoctorKpiDao doctorKpiDao,
                                             DoctorRangeReportDao doctorRangeReportDao,
                                             DoctorDailyReportDao doctorDailyReportDao,
                                             DoctorDailyGroupDao doctorDailyGroupDao) {
        this.doctorParityMonthlyReportDao = doctorParityMonthlyReportDao;
        this.doctorBoarMonthlyReportDao = doctorBoarMonthlyReportDao;
        this.doctorKpiDao = doctorKpiDao;
        this.doctorRangeReportDao = doctorRangeReportDao;
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyGroupDao = doctorDailyGroupDao;
    }

    @Override
    public Response<DoctorCommonReportTrendDto> findMonthlyReportTrendByFarmIdAndSumAt(Long farmId, String sumAt, Integer index) {
        try {
            Date date;

            //yyyy-MM-dd 格式, 说明是按照天查的, yyyy-MM 格式, 说明是按照月查的
            if (DateUtil.isYYYYMMDD(sumAt)) {
                date = DateUtil.toDate(sumAt);
            } else {
                date = getLastDay(DateUtil.toYYYYMM(sumAt));
            }

            String monthStr = DateUtil.getYearMonth(date);

            //如果查询未来的数据, 返回失败查询
            if (new DateTime(date).isAfter(DateUtil.getDateEnd(DateTime.now()))) {
                return Response.ok(failReportTrend(monthStr));
            }

            //查询月报结果, 如果没查到, 返回失败的结果
            DoctorRangeReport report = doctorRangeReportDao.findByRangeReport(farmId, ReportRangeType.MONTH.getValue(), monthStr);
            if (report == null) {
                return Response.ok(failReportTrend(monthStr));
            }


            //拼接趋势图
            return Response.ok(new DoctorCommonReportTrendDto(getDoctorCommonReportDto(report), getMonthlyReportByIndex(farmId, date, index), getParityMonthlyReportByIndex(farmId, date), getBoarMonthlyReportByIndex(farmId, date)));
        } catch (Exception e) {
            log.error("find monthly report by farmId and sumAt failed, farmId:{}, sumAt:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.ok(failReportTrend(DateUtil.getDateStr(new Date())));
        }
    }

    @Override
    public Response<DoctorCommonReportTrendDto> findWeeklyReportTrendByFarmIdAndSumAt(Long farmId, Integer year, Integer week, Integer index) {
        String weekStr = DateUtil.getYearWeek(year, week); //取周一代表一周

        try {
            //如果查询未来的数据, 返回失败查询
            if (weekStr.compareTo(DateUtil.getYearWeek(new Date())) == 1) {
                return Response.ok(failReportTrend(weekStr));
            }

            DoctorCommonReportDto reportDto;

            //查询周报结果, 如果没查到, 返回失败的结果
            DoctorRangeReport report = doctorRangeReportDao.findByRangeReport(farmId, ReportRangeType.WEEK.getValue(), weekStr);
            if (report == null) {
                return Response.ok(failReportTrend(weekStr));
            }

            DoctorCommonReportTrendDto reportTrendDto = new DoctorCommonReportTrendDto();
            reportTrendDto.setReport(getDoctorCommonReportDto(report));
            reportTrendDto.setReports(getWeeklyReportByIndex(farmId, report.getSumFrom(), index));
            return Response.ok(reportTrendDto);
        } catch (Exception e) {
            log.error("find weekly report by farmId and sumAt failed, farmId:{}, week:{}, cause:{}",
                    farmId, week, Throwables.getStackTraceAsString(e));
            return Response.ok(failReportTrend(weekStr));
        }
    }

    @Override
    public Response<List<DoctorCommonReportDto>> findMonthlyReports(@NotNull(message = "date.not.null") String sumAt) {
        try {
            //如果查询未来的数据, 返回失败查询
            if (new DateTime(DateUtil.toDate(sumAt)).isAfter(DateUtil.getDateEnd(DateTime.now()))) {
                return Response.fail("find.monthly.report.data.failed");
            }
            List<DoctorCommonReportDto> commonReportDtos = Lists.newArrayList();
            List<DoctorRangeReport> reports = doctorRangeReportDao.findBySumAt(ReportRangeType.MONTH.getValue(), sumAt);
            reports.forEach(report -> {
                commonReportDtos.add(getDoctorCommonReportDto(report));
            });
            return Response.ok(commonReportDtos);
        } catch (Exception e) {
            log.error("find.monthly.report.data.failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.monthly.report.data.failed");
        }
    }


    @Override
    public Response<List<DoctorGroupLiveStockDetailDto>> findEveryGroupInfo(@NotNull(message = "date.not.null") String sumAt) {
        try {
            //如果查询未来的数据, 返回失败查询
            if (new DateTime(DateUtil.toDate(sumAt)).isAfter(DateUtil.getDateEnd(DateTime.now()))) {
                return Response.fail("find every group info failed");
            }
            Map<Integer, String> pigTypeMap = Maps.newHashMap();
            for(PigType pigType : PigType.values()) {
                pigTypeMap.put(pigType.getValue(), pigType.getDesc());
            }
            return Response.ok(doctorKpiDao.getEveryGroupInfo(sumAt).stream().map(map -> {
                DoctorGroupLiveStockDetailDto detailDto = JSON_MAPPER.getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                        .convertValue(map, DoctorGroupLiveStockDetailDto.class);
                detailDto.setSumAt(DateUtil.toDate(sumAt));
                detailDto.setType(detailDto.getType() != null ? pigTypeMap.get(Integer.parseInt(detailDto.getType())) : null);
                return detailDto;
            }).collect(Collectors.toList()));

        } catch (Exception e) {
            log.error("find.every.group.info.failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find every group info failed");
        }
    }

    private DoctorCommonReportDto getDoctorCommonReportDto(DoctorRangeReport report) {
        DoctorCommonReportDto doctorCommonReportDto = new DoctorCommonReportDto();
        Long farmId = report.getFarmId();
        Date startAt = report.getSumFrom();
        Date endAt = report.getSumTo();
        doctorCommonReportDto.setFarmId(report.getFarmId());
        doctorCommonReportDto.setDate(report.getSumAt());
        DoctorBaseReport pigChangeReport = doctorDailyReportDao.findPigChangeSum(farmId, startAt, endAt);
        DoctorGroupChangeSum groupChangeSum = doctorDailyGroupDao.getGroupChangeSum(farmId, startAt, endAt);

        doctorCommonReportDto.setChangeReport(pigChangeReport);
        doctorCommonReportDto.setGroupChangeReport(groupChangeSum);
        doctorCommonReportDto.setIndicatorReport(report);

        return doctorCommonReportDto;
    }

    //后去月报趋势图
    private List<DoctorCommonReportDto> getMonthlyReportByIndex(Long farmId, Date date, Integer index) {
        return DateUtil.getBeforeMonthEnds(date, MoreObjects.firstNonNull(index, MONTH_INDEX)).stream()
                .map(month -> {
                    String sumAt = DateUtil.getYearMonth(month);

                    if (DateTime.now().getDayOfMonth() == 1 && DateUtil.inSameYearMonth(month, new Date())) {
                        DoctorCommonReportDto reportDto = new DoctorCommonReportDto();
                        reportDto.setDate(sumAt);
                        return reportDto;
                    }
                    DoctorRangeReport report = doctorRangeReportDao.findByRangeReport(farmId, ReportRangeType.MONTH.getValue(), sumAt);
                    if (report == null) {
                        return failReportDto(sumAt);
                    }
                    DoctorCommonReportDto dto = getDoctorCommonReportDto(report);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //获取周报趋势
    private List<DoctorCommonReportDto> getWeeklyReportByIndex(Long farmId, Date date, Integer index) {
        return DateUtil.getBeforeMonthEnds(date, MoreObjects.firstNonNull(index, WEEK_INDEX)).stream()
                .map(week -> {
                    String sumAt = DateUtil.getYearWeek(week);
                    DoctorRangeReport report = doctorRangeReportDao.findByRangeReport(farmId, ReportRangeType.MONTH.getValue(), sumAt);
                    if (report == null) {
                        return failReportDto(sumAt);
                    }
                    DoctorCommonReportDto dto = getDoctorCommonReportDto(report);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    private List<DoctorParityMonthlyReport> getParityMonthlyReportByIndex(Long farmId, Date date){
        return doctorParityMonthlyReportDao.findDoctorParityMonthlyReports(farmId, new DateTime(date).toString(DateUtil.YYYYMM));
    }

    private List<DoctorBoarMonthlyReport> getBoarMonthlyReportByIndex(Long farmId, Date date){
        return doctorBoarMonthlyReportDao.findDoctorBoarMonthlyReports(farmId, new DateTime(date).toString(DateUtil.YYYYMM));
    }

    //查询失败的报表
    private static DoctorCommonReportDto failReportDto(String date) {
        DoctorCommonReportDto dto = new DoctorCommonReportDto();
        dto.setFail(true);
        dto.setDate(date);
        return dto;
    }

    //查询失败的结果
    private static DoctorCommonReportTrendDto failReportTrend(String date) {
        return new DoctorCommonReportTrendDto(failReportDto(date), Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());
    }

    //获取月末
    private static Date getLastDay(Date date) {
        DateTime datetime = new DateTime(date);
        DateTime now = DateTime.now();
        //当月
        if (DateUtil.inSameYearMonth(datetime.toDate(), now.toDate())) {
            return now.withTimeAtStartOfDay().toDate();
        }
        return datetime.withDayOfMonth(1).plusMonths(1).plusDays(-1).toDate();
    }

    private static String getWeekStr(DateTime date) {
        return "第" + date.getWeekOfWeekyear() + "周(" + date.toString(DateUtil.DATE) + ")";
    }

}
