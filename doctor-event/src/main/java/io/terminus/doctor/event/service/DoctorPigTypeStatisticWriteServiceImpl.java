package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigTypeStatisticDao;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪只数统计表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */
@Slf4j
@Service
public class DoctorPigTypeStatisticWriteServiceImpl implements DoctorPigTypeStatisticWriteService {

    private final DoctorPigTypeStatisticDao doctorPigTypeStatisticDao;

    @Autowired
    public DoctorPigTypeStatisticWriteServiceImpl(DoctorPigTypeStatisticDao doctorPigTypeStatisticDao) {
        this.doctorPigTypeStatisticDao = doctorPigTypeStatisticDao;
    }

    @Override
    public Response<Long> createPigTypeStatistic(DoctorPigTypeStatistic pigTypeStatistic) {
        try {
            doctorPigTypeStatisticDao.create(pigTypeStatistic);
            return Response.ok(pigTypeStatistic.getId());
        } catch (Exception e) {
            log.error("create pigTypeStatistic failed, pigTypeStatistic:{}, cause:{}", pigTypeStatistic, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.create.fail");
        }
    }

    @Override
    public Response<Boolean> updatePigTypeStatisticById(DoctorPigTypeStatistic pigTypeStatistic) {
        try {
            return Response.ok(doctorPigTypeStatisticDao.update(pigTypeStatistic));
        } catch (Exception e) {
            log.error("update pigTypeStatistic failed, pigTypeStatistic:{}, cause:{}", pigTypeStatistic, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.update.fail");
        }
    }

    @Override
    public Response<Boolean> updatePigTypeStatisticByFarmId(DoctorPigTypeStatistic pigTypeStatistic) {
        try {
            return Response.ok(doctorPigTypeStatisticDao.updateByFarmId(pigTypeStatistic));
        } catch (Exception e) {
            log.error("update pigTypeStatistic failed, pigTypeStatistic:{}, cause:{}", pigTypeStatistic, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.update.fail");
        }
    }

    @Override
    public Response<Boolean> deletePigTypeStatisticById(Long pigTypeStatisticId) {
        try {
            return Response.ok(doctorPigTypeStatisticDao.delete(pigTypeStatisticId));
        } catch (Exception e) {
            log.error("delete pigTypeStatistic failed, pigTypeStatisticId:{}, cause:{}", pigTypeStatisticId, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.delete.fail");
        }
    }
}
