package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorBoarMonthlyReportDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.dao.DoctorParityMonthlyReportDao;
import io.terminus.doctor.event.dao.DoctorWeeklyReportDao;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportDto;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportTrendDto;
import io.terminus.doctor.event.dto.report.common.DoctorGroupLiveStockDetailDto;
import io.terminus.doctor.event.model.DoctorBoarMonthlyReport;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import io.terminus.doctor.event.model.DoctorParityMonthlyReport;
import io.terminus.doctor.event.model.DoctorWeeklyReport;
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

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;
    private final DoctorWeeklyReportDao doctorWeeklyReportDao;
    private final DoctorParityMonthlyReportDao doctorParityMonthlyReportDao;
    private final DoctorBoarMonthlyReportDao doctorBoarMonthlyReportDao;
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorCommonReportReadServiceImpl(DoctorMonthlyReportDao doctorMonthlyReportDao,
                                             DoctorWeeklyReportDao doctorWeeklyReportDao,
                                             DoctorParityMonthlyReportDao doctorParityMonthlyReportDao,
                                             DoctorBoarMonthlyReportDao doctorBoarMonthlyReportDao,
                                             DoctorKpiDao doctorKpiDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
        this.doctorWeeklyReportDao = doctorWeeklyReportDao;
        this.doctorParityMonthlyReportDao = doctorParityMonthlyReportDao;
        this.doctorBoarMonthlyReportDao = doctorBoarMonthlyReportDao;
        this.doctorKpiDao = doctorKpiDao;
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

            String monthStr = DateUtil.getDateStr(date);

            //如果查询未来的数据, 返回失败查询
            if (new DateTime(date).isAfter(DateUtil.getDateEnd(DateTime.now()))) {
                return Response.ok(failReportTrend(monthStr));
            }

            //查询月报结果, 如果没查到, 返回失败的结果
            DoctorMonthlyReport report = doctorMonthlyReportDao.findByFarmIdAndSumAt(farmId, date);
            if (report == null) {
                return Response.ok(failReportTrend(monthStr));
            }

            DoctorCommonReportDto reportDto = JSON_MAPPER.fromJson(report.getData(), DoctorCommonReportDto.class);
            if (reportDto == null) {
                return Response.ok(failReportTrend(monthStr));
            }

            //拼接趋势图
            return Response.ok(new DoctorCommonReportTrendDto(reportDto, getMonthlyReportByIndex(farmId, date, index), getParityMonthlyReportByIndex(farmId, date), getBoarMonthlyReportByIndex(farmId, date)));
        } catch (Exception e) {
            log.error("find monthly report by farmId and sumAt failed, farmId:{}, sumAt:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.ok(failReportTrend(DateUtil.getDateStr(new Date())));
        }
    }

    @Override
    public Response<DoctorCommonReportTrendDto> findWeeklyReportTrendByFarmIdAndSumAt(Long farmId, Integer year, Integer week, Integer index) {
        DateTime weekDateTime = withWeekOfYear(year, week);
        String weekStr = getWeekStr(weekDateTime.withDayOfWeek(1)); //取周一代表一周

        try {
            //如果查询未来的数据, 返回失败查询
            if (weekDateTime.isAfter(DateUtil.getDateEnd(DateTime.now()))) {
                return Response.ok(failReportTrend(weekStr));
            }

            DoctorCommonReportDto reportDto;

            //查询周报结果, 如果没查到, 返回失败的结果
            DoctorWeeklyReport report = doctorWeeklyReportDao.findByFarmIdAndSumAt(farmId, weekDateTime.withTimeAtStartOfDay().toDate());
            if (report == null) {
                return Response.ok(failReportTrend(weekStr));
            }
            reportDto = JSON_MAPPER.fromJson(report.getData(), DoctorCommonReportDto.class);
            if (reportDto == null) {
                return Response.ok(failReportTrend(weekStr));
            }

            DoctorCommonReportTrendDto reportTrendDto = new DoctorCommonReportTrendDto();
            reportTrendDto.setReport(reportDto);
            reportTrendDto.setReports(getWeeklyReportByIndex(farmId, weekDateTime, index));
            return Response.ok(reportTrendDto);
        } catch (Exception e) {
            log.error("find weekly report by farmId and sumAt failed, farmId:{}, week:{}, cause:{}",
                    farmId, week, Throwables.getStackTraceAsString(e));
            return Response.ok(failReportTrend(weekStr));
        }
    }

    @Override
    public Response<List<DoctorMonthlyReport>> findMonthlyReports(@NotNull(message = "date.not.null") String sumAt) {
        try {
            //如果查询未来的数据, 返回失败查询
            if (new DateTime(DateUtil.toDate(sumAt)).isAfter(DateUtil.getDateEnd(DateTime.now()))) {
                return Response.fail("find.monthly.report.data.failed");
            }
            return Response.ok(doctorMonthlyReportDao.findBySumAt(sumAt));
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
    //查询趋势图
    private List<DoctorCommonReportDto> getMonthlyReportByIndex(Long farmId, Date date, Integer index) {
        return DateUtil.getBeforeMonthEnds(date, MoreObjects.firstNonNull(index, MONTH_INDEX)).stream()
                .map(month -> {
                    String monthStr = DateUtil.getDateStr(month);

                    if (DateTime.now().getDayOfMonth() == 1 && DateUtil.inSameYearMonth(month, new Date())) {
                        DoctorCommonReportDto reportDto = new DoctorCommonReportDto();
                        reportDto.setDate(monthStr);
                        return reportDto;
                    }
                    DoctorMonthlyReport report = doctorMonthlyReportDao.findByFarmIdAndSumAt(farmId, Dates.startOfDay(month));
                    if (report == null || !StringUtils.hasText(report.getData())) {
                        return failReportDto(DateUtil.getDateStr(month));
                    }
                    DoctorCommonReportDto dto = JSON_MAPPER.fromJson(report.getData(), DoctorCommonReportDto.class);
                    if (dto == null) {
                        return failReportDto(monthStr);
                    }
                    dto.setDate(monthStr);        //填上月份, 供前台显示
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //获取日报趋势
    private List<DoctorCommonReportDto> getWeeklyReportByIndex(Long farmId, DateTime date, Integer index) {
        return DateUtil.getBeforeWeekEnds(date.toDate(), MoreObjects.firstNonNull(index, WEEK_INDEX)).stream()
                .map(week -> {
                    String weekStr = getWeekStr(new DateTime(week).withDayOfWeek(1));

                    DoctorWeeklyReport report = doctorWeeklyReportDao.findByFarmIdAndSumAt(farmId, Dates.startOfDay(week));
                    if (report == null || !StringUtils.hasText(report.getData())) {
                        return failReportDto(weekStr);
                    }
                    DoctorCommonReportDto dto = JSON_MAPPER.fromJson(report.getData(), DoctorCommonReportDto.class);
                    if (dto == null) {
                        return failReportDto(weekStr);
                    }
                    dto.setDate(weekStr);        //填上月份, 供前台显示
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

    /**
     * 获取指定年份和周的日期, 如果是未来时间，返回今天
     * @param year 年
     * @param week 周
     * @return 日期
     */
    private static DateTime withWeekOfYear(Integer year, Integer week) {
        if (year == null || week == null) {
            return DateUtil.getDateEnd(DateTime.now());
        }
        DateTime yearDate = new DateTime(year, 1, 1, 0, 0);

        while (true) {
            if (yearDate.getDayOfWeek() == 7) {
                break;
            }
            yearDate = yearDate.plusDays(1);
        }
        return new DateTime(DateUtil.weekEnd(yearDate.plusWeeks(week).toDate()));
    }
}
