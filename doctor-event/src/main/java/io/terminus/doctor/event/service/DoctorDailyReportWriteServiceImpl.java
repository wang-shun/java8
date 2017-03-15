package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.redis.DailyReport2UpdateDao;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Desc: 猪场日报表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportWriteServiceImpl implements DoctorDailyReportWriteService {

    private final DoctorDailyReportManager doctorDailyReportManager;
    private final DailyReport2UpdateDao dailyReport2UpdateDao;

    @Autowired
    public DoctorDailyReportWriteServiceImpl(DoctorDailyReportManager doctorDailyReportManager,
                                             DailyReport2UpdateDao dailyReport2UpdateDao) {
        this.doctorDailyReportManager = doctorDailyReportManager;
        this.dailyReport2UpdateDao = dailyReport2UpdateDao;
    }

    @Override
    public Response<Boolean> createDailyReports(List<Long> farmIds, Date sumAt) {
        try {
            Date startAt = Dates.startOfDay(sumAt);
            farmIds.forEach(farmId -> doctorDailyReportManager.realTimeDailyReports(farmId, startAt));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create dailyReport failed: sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.create.fail");
        }
    }
    @Override
    public Response<Boolean> createDailyReports(Date beginDate, Date endDate, Long farmId){
        try{
            beginDate = Dates.startOfDay(beginDate);
            endDate = Dates.startOfDay(endDate);
            while(!beginDate.after(endDate)){
                doctorDailyReportManager.realTimeDailyReports(farmId, beginDate);
                beginDate = new DateTime(beginDate).plusDays(1).toDate();
            }
            return Response.ok(Boolean.TRUE);
        }catch(Exception e) {
            log.error("updateHistoryDailyReport failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("update.history.daily.report.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDailyReport2Update(Long farmId){
        try{
            dailyReport2UpdateDao.deleteDailyReport2Update(farmId);
            return Response.ok(Boolean.TRUE);
        }catch(Exception e) {
            log.error("saveDailyReport2Update failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("save.daily.report.to.update.fail");
        }
    }

    @Override
    public Response<Boolean> createYesterdayAndTodayReports(@NotNull(message = "farmId.not.null") List<Long> farmIds) {
        log.info("daily reports job start, now is: {}, farmIds = {}", DateUtil.toDateTimeString(new Date()), farmIds);
        Boolean status = true;
        Date yesterday = new DateTime(Dates.startOfDay(new Date())).plusSeconds(-1).toDate();
        try{
            createDailyReports(farmIds, yesterday);
            createDailyReports(farmIds,new Date());
        }catch(Exception e){
            log.error("daily reports job failed, cause: {} ", Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.create.fail");
        }
        log.info("daily reports job start, now is: {}", DateUtil.toDateTimeString(new Date()), farmIds);
        return Response.ok(Boolean.TRUE);
    }
}
