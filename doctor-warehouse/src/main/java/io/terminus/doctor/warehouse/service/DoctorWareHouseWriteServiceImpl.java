package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.manager.DoctorWareHouseManager;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
@RpcProvider
public class DoctorWareHouseWriteServiceImpl implements DoctorWareHouseWriteService{

    private final DoctorWareHouseDao doctorWareHouseDao;

    private final DoctorWareHouseManager doctorWareHouseManager;

    @Autowired
    public DoctorWareHouseWriteServiceImpl(DoctorWareHouseDao doctorWareHouseDao,
                                           DoctorWareHouseManager doctorWareHouseManager){
        this.doctorWareHouseDao = doctorWareHouseDao;
        this.doctorWareHouseManager = doctorWareHouseManager;
    }

    @Override
    public Response<Long> createWareHouse(DoctorWareHouse doctorWareHouse) {
        try{
            // validate farmInfo
            if(isNull(doctorWareHouse.getFarmId())){
                return Response.fail("input.farmId.empty");
            }

            if(isNull(doctorWareHouse.getManagerId())){
                return Response.fail("input.manager.empty");
            }

            if(isNull(doctorWareHouse.getType())){
                return Response.fail("input.wareHouseType.empty");
            }

            checkState(doctorWareHouseDao.create(doctorWareHouse), "warehouse.create.fail");
            return Response.ok(doctorWareHouse.getId());
        }catch (Exception e){
            log.error("create ware house error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.wareHouse.error");
        }
    }

    @Override
    public Response<Boolean> updateWareHouse(DoctorWareHouse wareHouse) {
        try{
        	return Response.ok(this.doctorWareHouseManager.updateWareHouseInfo(wareHouse));
        }catch (IllegalStateException se){
            log.warn("update warehouse illegal state fail, warehouse:{}, cause:{}",wareHouse,  Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("update warehouse info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("update.warehouseInfo.fail");
        }
    }
}

