package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorCarouselFigureDao;
import io.terminus.doctor.basic.model.DoctorCarouselFigure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/17
 */
@Slf4j
@Service
public class DoctorCarouselFigureWriteServiceImpl implements DoctorCarouselFigureWriteService {

    private final DoctorCarouselFigureDao doctorCarouselFigureDao;

    @Autowired
    public DoctorCarouselFigureWriteServiceImpl(DoctorCarouselFigureDao doctorCarouselFigureDao) {
        this.doctorCarouselFigureDao = doctorCarouselFigureDao;
    }

    @Override
    public Response<Long> createFigure(DoctorCarouselFigure carouselFigure) {
        try {
            doctorCarouselFigureDao.create(carouselFigure);
            return Response.ok(carouselFigure.getId());
        } catch (Exception e) {
            log.error("figure create failed, carouselFigure:{}, cause:{}", carouselFigure, Throwables.getStackTraceAsString(e));
            return Response.fail("figure.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateFigure(DoctorCarouselFigure carouselFigure) {
        try {
            return Response.ok(doctorCarouselFigureDao.update(carouselFigure));
        } catch (Exception e) {
            log.error("figure update failed, carouselFigure:{}, cause:{}", carouselFigure, Throwables.getStackTraceAsString(e));
            return Response.fail("figure.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteFigure(Long figureId) {
        try {
            return Response.ok(doctorCarouselFigureDao.delete(figureId));
        } catch (Exception e) {
            log.error("figure delete failed, figureId:{}, cause:{}", figureId, Throwables.getStackTraceAsString(e));
            return Response.fail("figure.delete.fail");
        }
    }
}
