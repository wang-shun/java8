package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBreedDao;
import io.terminus.doctor.basic.model.DoctorBreed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 品种表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorBreedReadServiceImpl implements DoctorBreedReadService {

    private final DoctorBreedDao doctorBreedDao;

    @Autowired
    public DoctorBreedReadServiceImpl(DoctorBreedDao doctorBreedDao) {
        this.doctorBreedDao = doctorBreedDao;
    }

    @Override
    public Response<DoctorBreed> findBreedById(Long breedId) {
        try {
            return Response.ok(doctorBreedDao.findById(breedId));
        } catch (Exception e) {
            log.error("find breed by id failed, breedId:{}, cause:{}", breedId, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.find.fail");
        }
    }

}
