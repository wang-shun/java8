package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorDailyPigDao;
import io.terminus.doctor.event.model.DoctorDailyPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪数量每天记录表读服务实现类
 * Date: 2017-04-17
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyPigReadServiceImpl implements DoctorDailyPigReadService {

    private final DoctorDailyPigDao doctorDailyPigDao;

    @Autowired
    public DoctorDailyPigReadServiceImpl(DoctorDailyPigDao doctorDailyPigDao) {
        this.doctorDailyPigDao = doctorDailyPigDao;
    }

    @Override
    public Response<DoctorDailyPig> findDoctorDailyPigById(Long doctorDailyPigId) {
        try {
            return Response.ok(doctorDailyPigDao.findById(doctorDailyPigId));
        } catch (Exception e) {
            log.error("find doctorDailyPig by id failed, doctorDailyPigId:{}, cause:{}", doctorDailyPigId, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorDailyPig.find.fail");
        }
    }
}
