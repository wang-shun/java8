package io.terminus.doctor.event.service;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

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

    @Autowired
    public DoctorDailyReportReadServiceImpl(DoctorDailyReportDao doctorDailyReportDao,
                                            DoctorDailyReportCache doctorDailyReportCache) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
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

            //如果查未来的时间, 直接返回0
            if (date != null && date.after(new Date())) {
                log.info("search date not after now! date:{}", date);
                return Response.ok();
            }

            DoctorDailyReportDto report = doctorDailyReportCache.getDailyReport(farmId, date);
            if (report != null) {
                return Response.ok(report);
            }

            //如果缓存里不存在, 直接查数据库
            report = getDailyReportWithSql(farmId, date);

            //如果数据库里不存在, 重新计算
            if (report == null) {
                report = doctorDailyReportCache.initDailyReportByFarmIdAndDate(farmId, date);
            }

            //如果计算结果为空, 返回初始化的日报, 不放入缓存
            if (report == null) {
                return Response.ok(new DoctorDailyReportDto());
            }
            doctorDailyReportCache.putDailyReport(farmId, date, report);
            return Response.ok(report);
        } catch (Exception e) {
            log.error("find dailyReport by farm id and sumat fail, farmId:{}, sumat:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.find.fail");
        }
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

    //根据farmId和sumAt从数据库查询, 并转换成日报统计
    private DoctorDailyReportDto getDailyReportWithSql(Long farmId, Date sumAt) {
        DoctorDailyReport report = doctorDailyReportDao.findByFarmIdAndSumAt(farmId, sumAt);

        //如果没有查到, 要返回null, 交给上层判断
        if (report == null || Strings.isNullOrEmpty(report.getData())) {
            return null;
        }
        return report.getReportData();
    }
}
