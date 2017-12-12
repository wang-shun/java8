package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import io.terminus.doctor.event.manager.DoctorDailyReportV2Manager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportV2ServiceImpl implements DoctorDailyReportV2Service {

    private final DoctorDailyReportV2Manager doctorDailyReportV2Manager;

    @Autowired
    public DoctorDailyReportV2ServiceImpl(DoctorDailyReportV2Manager doctorDailyReportV2Manager) {
        this.doctorDailyReportV2Manager = doctorDailyReportV2Manager;
    }

    @Override
    public Response<Boolean> flushFarmDaily(Long farmId, String startAt, String endAt) {
        log.info("flush farm daily starting, farmId:{}, startAt:{}, endAt:{}", farmId, startAt, endAt);
        try {
            flushGroupDaily(farmId, startAt, endAt);
            flushPigDaily(farmId, startAt, endAt);
            log.info("flush farm daily end");
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("flush farm daily failed, farmId:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, startAt, endAt, Throwables.getStackTraceAsString(e));
        return Response.fail("flush.farm.daily.failed");
        }
    }

    @Override
    public Response<Boolean> flushGroupDaily(Long farmId, String startAt, String endAt) {
        log.info("flush group daily starting, farmId:{}, startAt:{}, endAt:{}", farmId, startAt, endAt);
        try {
            PigType.GROUP_TYPES.forEach(pigType -> flushGroupDaily(farmId, pigType, startAt, endAt));
            log.info("flush group daily end");
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("flush group daily failed, farmId:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, startAt , endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("flush.group.daily.failed");
        }
    }

    @Override
    public Response<Boolean> flushGroupDaily(Long farmId, Integer pigType, String startAt, String endAt) {
        log.info("flush farm daily for pigType starting, farmId:{}, pigType:{}, startAt:{}, endAt:{}", farmId, pigType, startAt, endAt);
        try {
            DoctorStatisticCriteria criteria = new DoctorStatisticCriteria();
            criteria.setFarmId(farmId);
            criteria.setPigType(pigType);
            List<Date> list = DateUtil.getDates(DateUtil.toDate(startAt), DateUtil.toDate(endAt));
            if (list.isEmpty()) {
                return Response.fail("startAt.or.endAt.is.error");
            }
            
            list.forEach(date -> {
                criteria.setSumAt(DateUtil.toDateString(date));
                doctorDailyReportV2Manager.flushGroupDaily(criteria);
            });
            log.info("flush group daily for pigType end");
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("flush group daily failed, farmId:{}, pigType:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, pigType, startAt , endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("flush.group.daily.failed");
        }
    }

    @Override
    public Response<Boolean> flushPigDaily(Long farmId, String startAt, String endAt) {
        // TODO: 17/12/12  
        return null;
    }
}
