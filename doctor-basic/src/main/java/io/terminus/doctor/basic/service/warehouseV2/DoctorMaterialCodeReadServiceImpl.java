package io.terminus.doctor.basic.service.warehouseV2;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorMaterialCodeDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorMaterialCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.ServerException;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/9/11.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMaterialCodeReadServiceImpl implements DoctorMaterialCodeReadService {

    @Autowired
    private DoctorMaterialCodeDao doctorMaterialCodeDao;

    @Override
    public Response<DoctorMaterialCode> find(Long warehouseId, Long materialId, String vendorName) {
        try {
            List<DoctorMaterialCode> codes = doctorMaterialCodeDao.list(DoctorMaterialCode.builder().build());
            if (null == codes||codes.isEmpty())
                return Response.ok(null);
            else return Response.ok(codes.get(0));
        } catch (Exception e) {
            log.error("failed to find doctor material code, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.material.code.find.fail");
        }
    }
}
