package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorUnitDao;
import io.terminus.doctor.basic.model.DoctorUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 计量单位表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorUnitWriteServiceImpl implements DoctorUnitWriteService {

    private final DoctorUnitDao doctorUnitDao;

    @Autowired
    public DoctorUnitWriteServiceImpl(DoctorUnitDao doctorUnitDao) {
        this.doctorUnitDao = doctorUnitDao;
    }

    @Override
    public Response<Long> createUnit(DoctorUnit unit) {
        try {
            doctorUnitDao.create(unit);
            return Response.ok(unit.getId());
        } catch (Exception e) {
            log.error("create unit failed, unit:{}, cause:{}", unit, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateUnit(DoctorUnit unit) {
        try {
            return Response.ok(doctorUnitDao.update(unit));
        } catch (Exception e) {
            log.error("update unit failed, unit:{}, cause:{}", unit, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteUnitById(Long unitId) {
        try {
            return Response.ok(doctorUnitDao.delete(unitId));
        } catch (Exception e) {
            log.error("delete unit failed, unitId:{}, cause:{}", unitId, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.delete.fail");
        }
    }
}
