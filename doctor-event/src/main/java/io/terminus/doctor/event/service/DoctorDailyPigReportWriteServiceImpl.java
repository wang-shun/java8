package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.report.DoctorDailyPigCountChain;
import io.terminus.doctor.event.report.DoctorDailyPigCountInvocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yaoqijun.
 * Date:2016-07-20
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorDailyPigReportWriteServiceImpl implements DoctorDailyPigReportWriteService{

    private final DoctorDailyPigCountInvocation doctorDailyPigCountInvocation;

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorDailyReportCache doctorDailyReportCache;

    @Autowired
    public DoctorDailyPigReportWriteServiceImpl(DoctorDailyPigCountInvocation doctorDailyPigCountInvocation,
                                                DoctorPigEventDao doctorPigEventDao,
                                                DoctorDailyReportCache doctorDailyReportCache){
        this.doctorDailyPigCountInvocation = doctorDailyPigCountInvocation;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
    }

    @Override
    public Response<Boolean> updateDailyPigReportInfo(Long pigEventId) {
        try{
            DoctorPigEvent doctorPigEvent = doctorPigEventDao.findById(pigEventId);
            doctorDailyReportCache.addDailyReport(doctorPigEvent.getFarmId(), doctorPigEvent.getEventAt(),
                    doctorDailyPigCountInvocation.countPigEvent(Lists.newArrayList(doctorPigEvent)));
        	return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException se){
            log.warn("update daily pig illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("update daily pig report fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("update.dailyReport.fail");
        }
    }
}
