package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.basic.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.basic.model.DoctorFarmWareHouseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yaoqijun.
 * Date:2016-07-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
@RpcProvider
public class DoctorWareHouseTypeWriteServiceImpl implements DoctorWareHouseTypeWriteService{

    private final DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    @Autowired
    public DoctorWareHouseTypeWriteServiceImpl(DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao){
        this.doctorFarmWareHouseTypeDao = doctorFarmWareHouseTypeDao;
    }

    @Override
    public Response<Boolean> initDoctorWareHouseType(Long farmId, String farmName, Long userId, String userName) {
        try{
            doctorFarmWareHouseTypeDao.create(buildFarmWareHouseType(farmId, farmName, userId, userName, WareHouseType.FEED));
            doctorFarmWareHouseTypeDao.create(buildFarmWareHouseType(farmId, farmName, userId, userName, WareHouseType.MATERIAL));
            doctorFarmWareHouseTypeDao.create(buildFarmWareHouseType(farmId, farmName, userId, userName, WareHouseType.CONSUME));
            doctorFarmWareHouseTypeDao.create(buildFarmWareHouseType(farmId, farmName, userId, userName, WareHouseType.VACCINATION));
            doctorFarmWareHouseTypeDao.create(buildFarmWareHouseType(farmId, farmName, userId, userName, WareHouseType.MEDICINE));

            return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException e){
            log.error("init doctor ware house type error , farmId:{}, farmName:{}, cause:{}",
                    farmId, farmName, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("init doctor ware house type error, farmId:{}, farmName:{}, cause:{}",farmId, farmName, Throwables.getStackTraceAsString(e));
            return Response.fail("init.doctorWareHosueType.fail");
        }
    }


    private DoctorFarmWareHouseType buildFarmWareHouseType(Long farmId, String farmName, Long userId, String userName, WareHouseType wareHouseType){
        return DoctorFarmWareHouseType.builder().farmId(farmId).farmName(farmName).type(wareHouseType.getKey())
                .lotNumber(0D).extraMap(Maps.newHashMap())
                .creatorId(userId).creatorName(userName)
                .build();
    }
}
