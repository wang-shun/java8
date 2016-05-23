package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBarnDao;
import io.terminus.doctor.basic.model.DoctorBarn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 猪舍表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorBarnReadServiceImpl implements DoctorBarnReadService {

    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorBarnReadServiceImpl(DoctorBarnDao doctorBarnDao) {
        this.doctorBarnDao = doctorBarnDao;
    }

    @Override
    public Response<DoctorBarn> findBarnById(Long barnId) {
        try {
            return Response.ok(doctorBarnDao.findById(barnId));
        } catch (Exception e) {
            log.error("find barn by id failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBarn>> findBarnsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorBarnDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find barn by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }
}
