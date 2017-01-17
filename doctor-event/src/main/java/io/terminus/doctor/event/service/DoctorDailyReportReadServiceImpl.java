package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.redis.DailyReport2UpdateDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.terminus.common.utils.Arguments.isEmpty;

/**
 * Desc: 猪场日报表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportReadServiceImpl implements DoctorDailyReportReadService {

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorDailyReportCache doctorDailyReportCache;
    private final DailyReport2UpdateDao dailyReport2UpdateDao;

    @Autowired
    public DoctorDailyReportReadServiceImpl(DoctorDailyReportDao doctorDailyReportDao,
                                            DoctorDailyReportCache doctorDailyReportCache,
                                            DailyReport2UpdateDao dailyReport2UpdateDao) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
        this.dailyReport2UpdateDao = dailyReport2UpdateDao;
    }

    //@PostConstruct
    public void init() {
        try {
            Date now = new Date();
            List<DoctorDailyReportDto> reportDtos = RespHelper.orServEx(initDailyReportByDate(now));
            reportDtos.forEach(report -> doctorDailyReportCache.putDailyReportToMySQL(report.getFarmId(), now, report));
            log.info("init daily report cache success!");
        } catch (ServiceException e) {
            log.error("init daily report failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public Response<DoctorDailyReportDto> findDailyReportByFarmIdAndSumAtWithCache(Long farmId, String sumAt) {
        try {
            Date date = DateUtil.toDate(sumAt);
            if(date == null){
                return Response.ok(failReport(sumAt));
            }
            DoctorDailyReport report = doctorDailyReportCache.getDailyReport(farmId, date);
            if(report == null || report.getReportData() == null){
                // 如果查当天的日报, 查不到就直接计算并存入redis, 如果查未来，返回失败查询
                if(!date.after(Dates.startOfDay(new Date()))){
                    DoctorDailyReportDto reportDto = doctorDailyReportCache.initDailyReportByFarmIdAndDate(farmId, date);
                    report = new DoctorDailyReport();
                    report.setFarmId(farmId);
                    report.setSumAt(date);
                    report.setReportData(reportDto);
                    report.setSowCount(reportDto.getSowCount());
                    report.setFarrowCount(reportDto.getLiveStock().getFarrow());
                    report.setNurseryCount(reportDto.getLiveStock().getNursery());
                    report.setFattenCount(reportDto.getLiveStock().getFatten());
                    doctorDailyReportDao.create(report);
                    return Response.ok(reportDto);
                }else{
                    return Response.ok(failReport(sumAt));
                }
            }else{
                return Response.ok(report.getReportData());
            }
        } catch (Exception e) {
            log.error("find dailyReport by farm id and sumat fail, farmId:{}, sumat:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.ok(failReport(sumAt));
        }
    }

    @Override
    public Response<List<DoctorDailyReportDto>> findDailyReportByFarmIdAndRangeWithCache(Long farmId, String startAt, String endAt) {
        try {
            Date end = isEmpty(endAt) ? new Date() : DateUtil.toDate(endAt);
            Date start = isEmpty(startAt) ? new DateTime(end).plusDays(-30).toDate() : DateUtil.toDate(startAt);

            List<DoctorDailyReportDto> report = Lists.newArrayList();
            while (!Dates.startOfDay(start).after(Dates.startOfDay(end))) {
                report.add(findDailyReportByFarmIdAndSumAtWithCache(farmId, DateUtil.toDateString(start)).getResult());
                start = new DateTime(start).plusDays(1).toDate();
            }
            return Response.ok(report);
        } catch (Exception e) {
            log.error("find dailyReport by farm id and range fail, farmId:{}, startAt:{}, endAt, cause:{}",
                    farmId, startAt, endAt, Throwables.getStackTraceAsString(e));
            return Response.ok(Lists.newArrayList());
        }
    }

    private DoctorDailyReportDto failReport(String sumat) {
        DoctorDailyReportDto dto = new DoctorDailyReportDto();
        dto.setFail(true);
        dto.setSumAt(DateUtil.toDate(sumat));
        return dto;
    }

    @Override
    public Response<List<DoctorDailyReportDto>> initDailyReportByDate(Date date) {
        try {
            return Response.ok(doctorDailyReportCache.initDailyReportByDate(date));
        } catch (Exception e) {
            log.error("init daily report failed, date:{}, cause:{}", date, Throwables.getStackTraceAsString(e));
            return Response.fail("init.daily.report.fail");
        }
    }

    /**
     * 根据日期和猪场id获取初始化的日报统计
     *
     * @param farmId 猪场id
     * @param date   日期
     * @return 日报统计
     */
    @Override
    public Response<DoctorDailyReportDto> initDailyReportByFarmIdAndDate(Long farmId, Date date) {
        try {
            return Response.ok(doctorDailyReportCache.initDailyReportByFarmIdAndDate(farmId, date));
        } catch (Exception e) {
            log.error("init daily report failed, date:{}, cause:{}", date, Throwables.getStackTraceAsString(e));
            return Response.fail("init.daily.report.fail");
        }
    }

    /**
     * 根据查询查询日报
     *
     * @param date 日期
     * @return 日报list
     */
    @Override
    public Response<List<DoctorDailyReport>> findDailyReportBySumAt(Date date) {
        try {
            return Response.ok(doctorDailyReportDao.findBySumAt(Dates.startOfDay(date)));
        } catch (Exception e) {
            log.error("find daily report by sumat failed, date:{}, cause:{}", date, Throwables.getStackTraceAsString(e));
            return Response.fail("daily.report.find.fail");
        }
    }

    @Override
    public Response<Map<Long, String>> getDailyReport2Update(){
        try{
            return Response.ok(dailyReport2UpdateDao.getDailyReport2Update());
        }catch(Exception e) {
            log.error("getDailyReport2Update failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("get.daily.report.to.update.fail");
        }
    }
}
