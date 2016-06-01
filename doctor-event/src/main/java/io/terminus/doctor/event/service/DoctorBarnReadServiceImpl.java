package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 猪舍表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorBarnReadServiceImpl implements DoctorBarnReadService {

    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorBarnReadServiceImpl(DoctorBarnDao doctorBarnDao) {
        this.doctorBarnDao = doctorBarnDao;
    }

    @Override
    public Response<DoctorBarn> findBarnById(Long barnId) {
        try {
            return Response.ok(doctorBarnDao.findById(barnId));
        } catch (Exception e) {
            log.error("find barn by id failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBarn>> findBarnsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorBarnDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find barn by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBarn>> findBarnsByEnums(Long farmId, Integer pigType, Integer canOpenGroup, Integer status) {
        try {
            return Response.ok(doctorBarnDao.findByEnums(farmId, pigType, canOpenGroup, status));
        } catch (Exception e) {
            log.error("find barn by enums fail, farmId:{}, pigType:{}, canOpenGroup:{}, status:{}, cause:{}",
                    farmId, pigType, canOpenGroup, status, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<Integer> countPigByBarnId(Long barnId) {
        return Response.ok(RandomUtil.random(1, 50));
    }

    @Override
    public Response<Boolean> checkBarnNameRepeat(Long farmId, String barnName) {
        try {
            return Response.ok(doctorBarnDao.findByFarmId(farmId).stream().anyMatch(barn -> barnName.equals(barn.getName())));
        } catch (Exception e) {
            log.error("check barn name repeat failed, farmId:{}, barnName:{}, cause:{}", farmId, barnName, Throwables.getStackTraceAsString(e));
            return Response.fail("check.repeat.fail");
        }
    }
}
