package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBreedDao;
import io.terminus.doctor.basic.model.DoctorBreed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 品种表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorBreedWriteServiceImpl implements DoctorBreedWriteService {

    private final DoctorBreedDao doctorBreedDao;

    @Autowired
    public DoctorBreedWriteServiceImpl(DoctorBreedDao doctorBreedDao) {
        this.doctorBreedDao = doctorBreedDao;
    }

    @Override
    public Response<Long> createBreed(DoctorBreed breed) {
        try {
            doctorBreedDao.create(breed);
            return Response.ok(breed.getId());
        } catch (Exception e) {
            log.error("create breed failed, breed:{}, cause:{}", breed, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBreed(DoctorBreed breed) {
        try {
            return Response.ok(doctorBreedDao.update(breed));
        } catch (Exception e) {
            log.error("update breed failed, breed:{}, cause:{}", breed, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBreedById(Long breedId) {
        try {
            return Response.ok(doctorBreedDao.delete(breedId));
        } catch (Exception e) {
            log.error("delete breed failed, breedId:{}, cause:{}", breedId, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.delete.fail");
        }
    }
}
