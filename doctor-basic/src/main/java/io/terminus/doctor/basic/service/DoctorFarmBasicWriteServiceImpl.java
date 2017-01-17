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
 * Desc: 猪场基础数据关联表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
@Slf4j
@Service
@RpcProvider
public class DoctorFarmBasicWriteServiceImpl implements DoctorFarmBasicWriteService {

    private final DoctorFarmBasicDao doctorFarmBasicDao;

    @Autowired
    public DoctorFarmBasicWriteServiceImpl(DoctorFarmBasicDao doctorFarmBasicDao) {
        this.doctorFarmBasicDao = doctorFarmBasicDao;
    }

    @Override
    public Response<Long> createFarmBasic(DoctorFarmBasic farmBasic) {
        try {
            doctorFarmBasicDao.create(farmBasic);
            return Response.ok(farmBasic.getId());
        } catch (Exception e) {
            log.error("create farmBasic failed, farmBasic:{}, cause:{}", farmBasic, Throwables.getStackTraceAsString(e));
            return Response.fail("farmBasic.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateFarmBasic(DoctorFarmBasic farmBasic) {
        try {
            return Response.ok(doctorFarmBasicDao.update(farmBasic));
        } catch (Exception e) {
            log.error("update farmBasic failed, farmBasic:{}, cause:{}", farmBasic, Throwables.getStackTraceAsString(e));
            return Response.fail("farmBasic.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteFarmBasicById(Long farmBasicId) {
        try {
            return Response.ok(doctorFarmBasicDao.delete(farmBasicId));
        } catch (Exception e) {
            log.error("delete farmBasic failed, farmBasicId:{}, cause:{}", farmBasicId, Throwables.getStackTraceAsString(e));
            return Response.fail("farmBasic.delete.fail");
        }
    }
}
