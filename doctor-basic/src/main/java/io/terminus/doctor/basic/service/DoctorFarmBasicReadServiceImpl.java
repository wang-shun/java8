package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorFarmBasicDao;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪场基础数据关联表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
@Slf4j
@Service
@RpcProvider
public class DoctorFarmBasicReadServiceImpl implements DoctorFarmBasicReadService {

    private final DoctorFarmBasicDao doctorFarmBasicDao;

    @Autowired
    public DoctorFarmBasicReadServiceImpl(DoctorFarmBasicDao doctorFarmBasicDao) {
        this.doctorFarmBasicDao = doctorFarmBasicDao;
    }

    @Override
    public Response<DoctorFarmBasic> findFarmBasicById(Long farmBasicId) {
        try {
            return Response.ok(doctorFarmBasicDao.findById(farmBasicId));
        } catch (Exception e) {
            log.error("find farmBasic by id failed, farmBasicId:{}, cause:{}", farmBasicId, Throwables.getStackTraceAsString(e));
            return Response.fail("farmBasic.find.fail");
        }
    }

    @Override
    public Response<DoctorFarmBasic> findFarmBasicByFarmId(Long farmId) {
        try {
            return Response.ok(doctorFarmBasicDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find farmBasic by farmId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("farmBasic.find.fail");
        }
    }
}
