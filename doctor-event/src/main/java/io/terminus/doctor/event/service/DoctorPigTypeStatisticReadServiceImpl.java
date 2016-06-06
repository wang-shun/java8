package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigTypeStatisticDao;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 猪只数统计表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */
@Slf4j
@Service
public class DoctorPigTypeStatisticReadServiceImpl implements DoctorPigTypeStatisticReadService {

    private final DoctorPigTypeStatisticDao doctorPigTypeStatisticDao;

    @Autowired
    public DoctorPigTypeStatisticReadServiceImpl(DoctorPigTypeStatisticDao doctorPigTypeStatisticDao) {
        this.doctorPigTypeStatisticDao = doctorPigTypeStatisticDao;
    }

    @Override
    public Response<DoctorPigTypeStatistic> findPigTypeStatisticByFarmId(Long farmId) {
        try {
            return Response.ok(doctorPigTypeStatisticDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find pigTypeStatistic by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.find.fail");
        }
    }

    @Override
    public Response<List<DoctorPigTypeStatistic>> findPigTypeStatisticsByOrgId(Long orgId) {
        try {
            return Response.ok(doctorPigTypeStatisticDao.findByOrgId(orgId));
        } catch (Exception e) {
            log.error("find pigTypeStatistic by org id fail, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.find.fail");
        }
    }
}
