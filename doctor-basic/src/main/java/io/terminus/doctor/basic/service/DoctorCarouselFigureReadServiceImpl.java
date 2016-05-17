package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorCarouselFigureDao;
import io.terminus.doctor.basic.model.DoctorCarouselFigure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/17
 */
@Slf4j
@Service
public class DoctorCarouselFigureReadServiceImpl implements DoctorCarouselFigureReadService {

    private final DoctorCarouselFigureDao doctorCarouselFigureDao;

    @Autowired
    public DoctorCarouselFigureReadServiceImpl(DoctorCarouselFigureDao doctorCarouselFigureDao) {
        this.doctorCarouselFigureDao = doctorCarouselFigureDao;
    }

    @Override
    public Response<List<DoctorCarouselFigure>> findFiguresByStatus(Integer status) {
        try {
            return Response.ok(doctorCarouselFigureDao.findByStatus(status));
        } catch (Exception e) {
            log.error("find figure by status failed, status:{}, cause:{}", status, Throwables.getStackTraceAsString(e));
            return Response.fail("figure.find.fail");
        }
    }

    @Override
    public Response<DoctorCarouselFigure> findFigureById(Long id) {
        try {
            return Response.ok(doctorCarouselFigureDao.findById(id));
        } catch (Exception e) {
            log.error("find figure by id failed, id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("figure.find.fail");
        }
    }
}
