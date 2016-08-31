package io.terminus.doctor.event.service;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.redis.DailyReport2UpdateDao;
import io.terminus.doctor.event.dao.redis.DailyReportHistoryDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private final DailyReportHistoryDao dailyReportHistoryDao;
    private final DailyReport2UpdateDao dailyReport2UpdateDao;

    @Autowired
    public DoctorDailyReportReadServiceImpl(DoctorDailyReportDao doctorDailyReportDao,
                                            DoctorDailyReportCache doctorDailyReportCache,
                                            DailyReportHistoryDao dailyReportHistoryDao,
                                            DailyReport2UpdateDao dailyReport2UpdateDao) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
        this.dailyReportHistoryDao = dailyReportHistoryDao;
        this.dailyReport2UpdateDao = dailyReport2UpdateDao;
    }

    @PostConstruct
    public void init() {
        try {
            Date now = new Date();
            List<DoctorDailyReportDto> reportDtos = RespHelper.orServEx(initDailyReportByDate(now));
            reportDtos.forEach(report -> doctorDailyReportCache.putDailyReport(report.getFarmId(), now, report));
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
                return Response.ok(failReport());
            }
            DoctorDailyReportDto report = dailyReportHistoryDao.getDailyReportWithRedis(farmId, date);
            if(report == null){
                // 如果查当天的日报, 查不到就直接计算并存入redis
                if(date.equals(Dates.startOfDay(new Date()))){
                    report = doctorDailyReportCache.initDailyReportByFarmIdAndDate(farmId, date);
                    dailyReportHistoryDao.saveDailyReport(report, farmId, date);
                    return Response.ok(report);
                }else{
                    return Response.ok(failReport());
                }
            }else{
                return Response.ok(report);
            }
        } catch (Exception e) {
            log.error("find dailyReport by farm id and sumat fail, farmId:{}, sumat:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.ok(failReport());
        }
    }

    private DoctorDailyReportDto failReport() {
        DoctorDailyReportDto dto = new DoctorDailyReportDto();
        dto.setFail(true);
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

    @Override
    public Response<Boolean> clearAllReportCache() {
        try {
            doctorDailyReportCache.clearAllReport();
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("clear report cache failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
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

    //根据farmId和sumAt从数据库查询, 并转换成日报统计
    private DoctorDailyReportDto getDailyReportWithSql(Long farmId, Date sumAt) {
        DoctorDailyReport report = doctorDailyReportDao.findByFarmIdAndSumAt(farmId, sumAt);

        //如果没有查到, 要返回null, 交给上层判断
        if (report == null || Strings.isNullOrEmpty(report.getData())) {
            return null;
        }
        return report.getReportData();
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
