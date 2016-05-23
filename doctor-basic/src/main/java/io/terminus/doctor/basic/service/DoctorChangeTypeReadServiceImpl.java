package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.model.DoctorChangeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 变动类型表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorChangeTypeReadServiceImpl implements DoctorChangeTypeReadService {

    private final DoctorChangeTypeDao doctorChangeTypeDao;

    @Autowired
    public DoctorChangeTypeReadServiceImpl(DoctorChangeTypeDao doctorChangeTypeDao) {
        this.doctorChangeTypeDao = doctorChangeTypeDao;
    }

    @Override
    public Response<DoctorChangeType> findChangeTypeById(Long changeTypeId) {
        try {
            return Response.ok(doctorChangeTypeDao.findById(changeTypeId));
        } catch (Exception e) {
            log.error("find changeType by id failed, changeTypeId:{}, cause:{}", changeTypeId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.find.fail");
        }
    }

    @Override
    public Response<List<DoctorChangeType>> findChangeTypesByFarmId(Long farmId) {
        try {
            return Response.ok(doctorChangeTypeDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find changeType by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.find.fail");
        }
    }
}
