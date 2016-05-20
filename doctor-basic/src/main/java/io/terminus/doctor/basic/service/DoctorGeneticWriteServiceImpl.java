package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorGeneticDao;
import io.terminus.doctor.basic.model.DoctorGenetic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 品系表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGeneticWriteServiceImpl implements DoctorGeneticWriteService {

    private final DoctorGeneticDao doctorGeneticDao;

    @Autowired
    public DoctorGeneticWriteServiceImpl(DoctorGeneticDao doctorGeneticDao) {
        this.doctorGeneticDao = doctorGeneticDao;
    }

    @Override
    public Response<Long> createGenetic(DoctorGenetic genetic) {
        try {
            doctorGeneticDao.create(genetic);
            return Response.ok(genetic.getId());
        } catch (Exception e) {
            log.error("create genetic failed, genetic:{}, cause:{}", genetic, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGenetic(DoctorGenetic genetic) {
        try {
            return Response.ok(doctorGeneticDao.update(genetic));
        } catch (Exception e) {
            log.error("update genetic failed, genetic:{}, cause:{}", genetic, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGeneticById(Long geneticId) {
        try {
            return Response.ok(doctorGeneticDao.delete(geneticId));
        } catch (Exception e) {
            log.error("delete genetic failed, geneticId:{}, cause:{}", geneticId, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.delete.fail");
        }
    }
}
