package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorProfitMaterOrPigDao;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

/**
 * Created by terminus on 2017/4/12.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorProfitMaterOrPigReadServerImpl implements DoctorProfitMaterOrPigReadServer{

    private final DoctorProfitMaterOrPigDao doctorProfitMaterOrPigDao;
    @Autowired
    public DoctorProfitMaterOrPigReadServerImpl(DoctorProfitMaterOrPigDao doctorProfitMaterOrPigDao) {
        this.doctorProfitMaterOrPigDao = doctorProfitMaterOrPigDao;
    }

    @Override
    public Response<List<DoctorProfitMaterialOrPig>> findProfitMaterialOrPig(Long farmId, Map<String, Object> map) {
        try{
            return Response.ok(doctorProfitMaterOrPigDao.findProfitMaterialOrPig(farmId, map));
        }catch (Exception e) {
            log.error("find.profit.fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find profit fail");
        }
    }
}
