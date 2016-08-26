package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
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

    private static final String REDIS_KEY = "dailyReport:update:";

    private final DoctorDailyReportManager doctorDailyReportManager;
    private final JedisTemplate jedisTemplate;

    @Autowired
    public DoctorDailyReportWriteServiceImpl(DoctorDailyReportManager doctorDailyReportManager,
                                             JedisTemplate jedisTemplate) {
        this.doctorDailyReportManager = doctorDailyReportManager;
        this.jedisTemplate = jedisTemplate;
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
    public Response saveDailyReport2Update(Date beginDate, Long farmId){
        try{
            jedisTemplate.execute(jedis -> {
                jedis.set(REDIS_KEY + farmId, DateUtil.toDateString(beginDate));
            });
            return Response.ok();
        }catch(Exception e) {
            log.error("saveDailyReport2Update failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("save.daily.report.to.update.fail");
        }
    }

    @Override
    public Response<Map<Long, String>> getDailyReport2Update(){
        Map<Long, String> farmAndDate = new HashMap<>();
        try{
            jedisTemplate.execute(jedis -> {
                for(String key : jedis.keys(REDIS_KEY + "*")){
                    farmAndDate.put(Long.valueOf(key.replace(REDIS_KEY, "")), jedis.get(key));
                }
            });

            return Response.ok(farmAndDate);
        }catch(Exception e) {
            log.error("getDailyReport2Update failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("get.daily.report.to.update.fail");
        }
    }

}
