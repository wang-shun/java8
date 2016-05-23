package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorUnitDao;
import io.terminus.doctor.basic.model.DoctorUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 计量单位表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorUnitReadServiceImpl implements DoctorUnitReadService {

    private final DoctorUnitDao doctorUnitDao;

    @Autowired
    public DoctorUnitReadServiceImpl(DoctorUnitDao doctorUnitDao) {
        this.doctorUnitDao = doctorUnitDao;
    }

    @Override
    public Response<DoctorUnit> findUnitById(Long unitId) {
        try {
            return Response.ok(doctorUnitDao.findById(unitId));
        } catch (Exception e) {
            log.error("find unit by id failed, unitId:{}, cause:{}", unitId, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.find.fail");
        }
    }

}
