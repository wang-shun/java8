package io.terminus.doctor.basic.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.TestDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName TestServiceImpl
 * @Description TODO
 * @Author Danny
 * @Date 2018/7/4 11:06
 */
@Service
@RpcProvider
public class TestServiceImpl implements TestService {

    @Autowired
    private TestDao testDao;

    @Override
    public Response select(Integer id) {
        return Response.ok(testDao.findById(id));
    }

    @Override
    public Response add(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        return Response.ok(testDao.create(doctorWarehouseMaterialHandle));
    }

    @Override
    public Response update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        return Response.ok(testDao.update(doctorWarehouseMaterialHandle));
    }

    @Override
    public Response delete(Long id) {
        return Response.ok(testDao.delete(id));
    }

}
