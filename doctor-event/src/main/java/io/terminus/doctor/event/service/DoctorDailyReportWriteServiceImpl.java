package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.event.dao.redis.DailyReport2UpdateDao;
import io.terminus.doctor.event.dao.redis.DailyReportHistoryDao;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private final DailyReportHistoryDao dailyReportHistoryDao;

    @Autowired
    public DoctorDailyReportWriteServiceImpl(DoctorDailyReportManager doctorDailyReportManager,
                                             DailyReport2UpdateDao dailyReport2UpdateDao,
                                             DailyReportHistoryDao dailyReportHistoryDao) {
        this.doctorDailyReportManager = doctorDailyReportManager;
        this.dailyReport2UpdateDao = dailyReport2UpdateDao;
        this.dailyReportHistoryDao = dailyReportHistoryDao;
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
    public Response<Boolean> updateDailyReport(Date beginDate, Date endDate, Long farmId){
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
    public Response deleteDailyReport2Update(Long farmId){
        try{
            dailyReport2UpdateDao.deleteDailyReport2Update(farmId);
            return Response.ok();
        }catch(Exception e) {
            log.error("saveDailyReport2Update failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("save.daily.report.to.update.fail");
        }
    }

    @Override
    public Response deleteDailyReportFromRedis(Long farmId){
        try{
            dailyReportHistoryDao.deleteDailyReport(farmId);
            return Response.ok();
        }catch(Exception e){
            log.error("deleteDailyReport failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("delete.daily.report.redis.fail");
        }
    }

}
