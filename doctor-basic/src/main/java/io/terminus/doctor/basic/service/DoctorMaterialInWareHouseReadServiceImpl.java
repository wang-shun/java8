package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
@RpcProvider
public class DoctorMaterialInWareHouseReadServiceImpl implements DoctorMaterialInWareHouseReadService{

    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    private final DoctorWareHouseDao doctorWareHouseDao;

    @Autowired
    public DoctorMaterialInWareHouseReadServiceImpl(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,
                                                    DoctorWareHouseDao doctorWareHouseDao){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorWareHouseDao = doctorWareHouseDao;
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

    @Override
    public Response<Paging<DoctorMaterialInWareHouse>> pagingDoctorMaterialInWareHouse(
            Long farmId, Long wareHouseId, Long materialId, String materialName, Integer pageNo, Integer pageSize) {
        try{
            Map<String,Object> params = Maps.newHashMap();
            params.put("farmId", farmId);
            params.put("wareHouseId", wareHouseId);
            if(materialId != null){
                params.put("materialId", materialId);
            }
            if(StringUtils.isNotBlank(materialName)){
                params.put("materialName", materialName.trim());
            }
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            Paging<DoctorMaterialInWareHouse> paging = doctorMaterialInWareHouseDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), params);
        	return Response.ok(paging);
        }catch (Exception e){
            log.error("paging doctor material in wareHouse fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.wareHouse.fail");
        }
    }

    @Override
    public Response<DoctorMaterialInWareHouse> queryByMaterialWareHouseIds(Long farmId, Long materialId, Long wareHouseId) {
        try{
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(farmId, wareHouseId,materialId);
            return Response.ok(doctorMaterialInWareHouse);
        }catch (Exception e){
            log.error("query by farm material ware house ids fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.byIds.fail");
        }
    }

    @Override
    public Response<DoctorMaterialInWareHouse> queryDoctorMaterialInWareHouse(@NotNull(message = "input.id.empty") Long id) {
        try {
            return Response.ok(doctorMaterialInWareHouseDao.findById(id));
        }catch (Exception e){
            log.error("query doctor material in warehouse, id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("query.byId.fail");
        }
    }

    @Override
    public Response<DoctorMaterialInWareHouse> findMaterialUnits(@NotNull(message = "input.farmId.empty") Long farmId,
                                                                 @NotNull(message = "input.materialId.empty") Long materialId,
                                                                 @NotNull(message = "input.wareHouseId.empty") Long wareHouseId) {
        try {
            return Response.ok(doctorMaterialInWareHouseDao.findMaterialUnit(farmId, materialId, wareHouseId));
        }catch (Exception e) {
            log.error("find material unit fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find material unit fail");
        }
    }

}
