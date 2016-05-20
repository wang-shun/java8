package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorDiseaseDao;
import io.terminus.doctor.basic.model.DoctorDisease;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 变动类型表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorDiseaseWriteServiceImpl implements DoctorDiseaseWriteService {

    private final DoctorDiseaseDao doctorDiseaseDao;

    @Autowired
    public DoctorDiseaseWriteServiceImpl(DoctorDiseaseDao doctorDiseaseDao) {
        this.doctorDiseaseDao = doctorDiseaseDao;
    }

    @Override
    public Response<Long> createDisease(DoctorDisease disease) {
        try {
            doctorDiseaseDao.create(disease);
            return Response.ok(disease.getId());
        } catch (Exception e) {
            log.error("create disease failed, disease:{}, cause:{}", disease, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateDisease(DoctorDisease disease) {
        try {
            return Response.ok(doctorDiseaseDao.update(disease));
        } catch (Exception e) {
            log.error("update disease failed, disease:{}, cause:{}", disease, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDiseaseById(Long diseaseId) {
        try {
            return Response.ok(doctorDiseaseDao.delete(diseaseId));
        } catch (Exception e) {
            log.error("delete disease failed, diseaseId:{}, cause:{}", diseaseId, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.delete.fail");
        }
    }
}
