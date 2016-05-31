package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialInWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Response<Paging<DoctorMaterialInWareHouseDto>> pagingDoctorMaterialInWareHouse(Long farmId, Long wareHouseId, Integer pageNo, Integer pageSize) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);

            Map<String,Object> params = Maps.newHashMap();
            params.put("farmId", farmId);
            params.put("wareHouseId", wareHouseId);
            Paging<DoctorMaterialInWareHouse> doctorMaterialInWareHousePaging = doctorMaterialInWareHouseDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), params);

            if(doctorMaterialInWareHousePaging.isEmpty()){
                return Response.ok(Paging.<DoctorMaterialInWareHouseDto>empty());
            }

            List<DoctorMaterialInWareHouse> data = doctorMaterialInWareHousePaging.getData();

            List<DoctorWareHouse> wareHouses =  doctorWareHouseDao.findByIds(data.stream().map(d -> d.getWareHouseId()).collect(Collectors.toList()));
            Map<Long, DoctorWareHouse> doctorWareHouseMap = wareHouses.stream().collect(Collectors.toMap(k->k.getId(), v->v));

        	return Response.ok(new Paging<>(doctorMaterialInWareHousePaging.getTotal(),
                    data.stream().map(d->DoctorMaterialInWareHouseDto.buildDoctorMaterialInWareHouseInfo(d,doctorWareHouseMap.get(d.getWareHouseId()))).collect(Collectors.toList())));
        }catch (IllegalStateException se){
            log.warn("paging material in ware house illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
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

}
