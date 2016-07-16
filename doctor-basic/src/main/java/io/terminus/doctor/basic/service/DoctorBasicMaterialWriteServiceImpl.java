package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 基础物料表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Slf4j
@Service
@RpcProvider
public class DoctorBasicMaterialWriteServiceImpl implements DoctorBasicMaterialWriteService {

    private final DoctorBasicMaterialDao doctorBasicMaterialDao;

    @Autowired
    public DoctorBasicMaterialWriteServiceImpl(DoctorBasicMaterialDao doctorBasicMaterialDao) {
        this.doctorBasicMaterialDao = doctorBasicMaterialDao;
    }

    @Override
    public Response<Long> createBasicMaterial(DoctorBasicMaterial basicMaterial) {
        try {
            doctorBasicMaterialDao.create(basicMaterial);
            return Response.ok(basicMaterial.getId());
        } catch (Exception e) {
            log.error("create basicMaterial failed, basicMaterial:{}, cause:{}", basicMaterial, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBasicMaterial(DoctorBasicMaterial basicMaterial) {
        try {
            return Response.ok(doctorBasicMaterialDao.update(basicMaterial));
        } catch (Exception e) {
            log.error("update basicMaterial failed, basicMaterial:{}, cause:{}", basicMaterial, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBasicMaterialById(Long basicMaterialId) {
        try {
            return Response.ok(doctorBasicMaterialDao.delete(basicMaterialId));
        } catch (Exception e) {
            log.error("delete basicMaterial failed, basicMaterialId:{}, cause:{}", basicMaterialId, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.delete.fail");
        }
    }
}
