package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorGeneticDao;
import io.terminus.doctor.basic.model.DoctorGenetic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 品系表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGeneticReadServiceImpl implements DoctorGeneticReadService {

    private final DoctorGeneticDao doctorGeneticDao;

    @Autowired
    public DoctorGeneticReadServiceImpl(DoctorGeneticDao doctorGeneticDao) {
        this.doctorGeneticDao = doctorGeneticDao;
    }

    @Override
    public Response<DoctorGenetic> findGeneticById(Long geneticId) {
        try {
            return Response.ok(doctorGeneticDao.findById(geneticId));
        } catch (Exception e) {
            log.error("find genetic by id failed, geneticId:{}, cause:{}", geneticId, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.find.fail");
        }
    }

}
