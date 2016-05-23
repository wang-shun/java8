package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorDiseaseDao;
import io.terminus.doctor.basic.model.DoctorDisease;
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
public class DoctorDiseaseReadServiceImpl implements DoctorDiseaseReadService {

    private final DoctorDiseaseDao doctorDiseaseDao;

    @Autowired
    public DoctorDiseaseReadServiceImpl(DoctorDiseaseDao doctorDiseaseDao) {
        this.doctorDiseaseDao = doctorDiseaseDao;
    }

    @Override
    public Response<DoctorDisease> findDiseaseById(Long diseaseId) {
        try {
            return Response.ok(doctorDiseaseDao.findById(diseaseId));
        } catch (Exception e) {
            log.error("find disease by id failed, diseaseId:{}, cause:{}", diseaseId, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.find.fail");
        }
    }

    @Override
    public Response<List<DoctorDisease>> findDiseasesByFarmId(Long farmId) {
        try {
            return Response.ok(doctorDiseaseDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find disease by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.find.fail");
        }
    }
}
