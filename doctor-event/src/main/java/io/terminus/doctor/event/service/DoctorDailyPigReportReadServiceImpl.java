package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.report.DoctorDailyPigCountInvocation;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by yaoqijun.
 * Date:2016-07-20
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
public class DoctorDailyPigReportReadServiceImpl implements DoctorDailyPigReportReadService{

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorDailyPigCountInvocation doctorDailyPigCountInvocation;

    @Autowired
    public DoctorDailyPigReportReadServiceImpl(DoctorPigEventDao doctorPigEventDao, DoctorDailyPigCountInvocation doctorDailyPigCountInvocation){
        this.doctorDailyPigCountInvocation = doctorDailyPigCountInvocation;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    @Override
    public Response<DoctorDailyReportDto> countByFarmIdDate(Long farmId, Date sumAt) {
        try{
            DateTime dateTime = new DateTime(sumAt);
            Map<String,Object> params = Maps.newHashMap();
            params.put("farmId",farmId);
            params.put("beginDate", dateTime.withTimeAtStartOfDay().toString());
            params.put("endDate", dateTime.plusDays(1).withTimeAtStartOfDay());
            List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.list(params);

        	return Response.ok(doctorDailyPigCountInvocation.countPigEvent(doctorPigEvents));
        }catch (IllegalStateException se){
            log.warn("count by farmId date illegal state fail, farmId:{}, date:{}, cause:{}",farmId, sumAt, Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("count by farmId date fail, farmId:{}, sumAt:{}, cause:{}", farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("countDaily.farmIdDate.fail");
        }
    }

    @Override
    public Response<List<DoctorDailyReportDto>> countByDate(Date sumAt) {
        try{
            List<Long> farmIds = doctorPigEventDao.queryAllFarmInEvent();

            List<DoctorDailyReportDto> results = farmIds.stream().map(farmId-> RespHelper.orServEx(countByFarmIdDate(farmId, sumAt))).collect(Collectors.toList());

        	return Response.ok(results);
        }catch (IllegalStateException se){
            log.warn("illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("count By daily fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("dailyCount.pigSumAt.fail");
        }
    }

    @Override
    public Response<DoctorDailyReportDto> countSinglePigEvent(DoctorPigEvent doctorPigEvent) {
        try{
        	return Response.ok(doctorDailyPigCountInvocation.countPigEvent(Lists.newArrayList(doctorPigEvent)));
        }catch (IllegalStateException se){
            log.warn("daily single event count illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("daily single event count fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("dailyCount.singleEvent.fail");
        }
    }
}
