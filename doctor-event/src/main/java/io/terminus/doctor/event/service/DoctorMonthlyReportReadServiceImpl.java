package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportDto;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportTrendDto;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc: 猪场月报表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMonthlyReportReadServiceImpl implements DoctorMonthlyReportReadService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();
    private static final int MONTH_INDEX = 12;

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;

    @Autowired
    public DoctorMonthlyReportReadServiceImpl(DoctorMonthlyReportDao doctorMonthlyReportDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
    }

    @Override
    public Response<DoctorMonthlyReportTrendDto> findMonthlyReportTrendByFarmIdAndSumAt(Long farmId, String sumAt, Integer index) {
        try {
            Date date;

            //yyyy-MM-dd 格式, 说明是按照天查的, yyyy-MM 格式, 说明是按照月查的
            if (DateUtil.isYYYYMMDD(sumAt)) {
                date = DateUtil.toDate(sumAt);
            } else {
                date = getLastDay(DateUtil.toYYYYMM(sumAt));
            }

            //如果查询未来的数据, 返回失败查询
            if (new DateTime(date).isAfter(DateUtil.getDateEnd(DateTime.now()))) {
                return Response.ok(failReportTrend(date));
            }

            DoctorMonthlyReportDto reportDto;

            // 如果当前日期是1号, 并且查询的月份是当月, 则返回 0 月报
            if(DateTime.now().getDayOfMonth() == 1 && DateUtil.inSameYearMonth(date, new Date())){
                reportDto = new DoctorMonthlyReportDto();
                reportDto.setDate(DateUtil.getDateStr(date));
            }else{
                //查询月报结果, 如果没查到, 返回失败的结果
                DoctorMonthlyReport report = doctorMonthlyReportDao.findByFarmIdAndSumAt(farmId, date);
                if (report == null) {
                    return Response.ok(failReportTrend(date));
                }
                reportDto = JSON_MAPPER.fromJson(report.getData(), DoctorMonthlyReportDto.class);
                if (reportDto == null) {
                    return Response.ok(failReportTrend(date));
                }
            }

            //拼接趋势图
            return Response.ok(new DoctorMonthlyReportTrendDto(reportDto, getMonthlyReportByIndex(farmId, date, index)));
        } catch (Exception e) {
            log.error("find monthly report by farmId and sumAt failed, farmId:{}, sumAt:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.ok(failReportTrend(new Date()));
        }
    }

    //查询趋势图
    private List<DoctorMonthlyReportDto> getMonthlyReportByIndex(Long farmId, Date date, Integer index) {
        return DateUtil.getBeforeMonthEnds(date, MoreObjects.firstNonNull(index, MONTH_INDEX)).stream()
                .map(month -> {
                    if (DateTime.now().getDayOfMonth() == 1 && DateUtil.inSameYearMonth(month, new Date())) {
                        DoctorMonthlyReportDto reportDto = new DoctorMonthlyReportDto();
                        reportDto.setDate(DateUtil.getDateStr(month));
                        return reportDto;
                    }
                    DoctorMonthlyReport report = doctorMonthlyReportDao.findByFarmIdAndSumAt(farmId, Dates.startOfDay(month));
                    if (report == null || !StringUtils.hasText(report.getData())) {
                        return failReportDto(month);
                    }
                    DoctorMonthlyReportDto dto = JSON_MAPPER.fromJson(report.getData(), DoctorMonthlyReportDto.class);
                    if (dto == null) {
                        return failReportDto(month);
                    }
                    dto.setDate(DateUtil.getDateStr(month));        //填上月份, 供前台显示
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //查询失败的月报
    private static DoctorMonthlyReportDto failReportDto(Date date) {
        DoctorMonthlyReportDto dto = new DoctorMonthlyReportDto();
        dto.setFail(true);
        dto.setDate(DateUtil.getDateStr(date));
        return dto;
    }

    //查询失败的结果
    private static DoctorMonthlyReportTrendDto failReportTrend(Date date) {
        return new DoctorMonthlyReportTrendDto(failReportDto(date), Lists.newArrayList());
    }

    //获取月末
    private static Date getLastDay(Date date) {
        DateTime datetime = new DateTime(date);
        DateTime now = DateTime.now();
        //当月
        if (DateUtil.inSameYearMonth(datetime.toDate(), now.toDate())) {
            if(now.getDayOfMonth() == 1){
                return now.withTimeAtStartOfDay().toDate();
            }else{
                return now.withTimeAtStartOfDay().plusDays(-1).toDate();
            }
        }
        return datetime.withDayOfMonth(1).plusMonths(1).plusDays(-1).toDate();
    }
}
