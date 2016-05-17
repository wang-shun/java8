package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
public class DoctorMaterialInWareHouseReadServiceImpl implements DoctorMaterialInWareHouseReadService{

    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    public DoctorMaterialInWareHouseReadServiceImpl(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
    }

    @Override
    public Response<List<DoctorMaterialInWareHouse>> queryDoctorMaterialInWareHouse(Long farmId, Long wareHouseId) {
        try{
            return Response.ok(doctorMaterialInWareHouseDao.queryByFarmAndWareHouseId(farmId, wareHouseId));
        }catch (Exception e){
            log.error("query doctor material in ware house fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.materialWareHouse.fail");
        }
    }
}
