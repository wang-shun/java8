package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe: 仓库信息的读入操作
 */
@Service
@Slf4j
@RpcProvider
public class DoctorWareHouseReadServiceImpl implements DoctorWareHouseReadService{

    private final DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    private final DoctorWareHouseDao doctorWareHouseDao;

    private final DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    @Autowired
    public DoctorWareHouseReadServiceImpl(DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao,
                                          DoctorWareHouseDao doctorWareHouseDao,
                                          DoctorWareHouseTrackDao doctorWareHouseTrackDao){
        this.doctorFarmWareHouseTypeDao = doctorFarmWareHouseTypeDao;
        this.doctorWareHouseDao = doctorWareHouseDao;
        this.doctorWareHouseTrackDao = doctorWareHouseTrackDao;
    }

    @Override
    public Response<List<DoctorFarmWareHouseType>> queryDoctorFarmWareHouseType(Long farmId) {
        try{
            return Response.ok(doctorFarmWareHouseTypeDao.findByFarmId(farmId));
        }catch (Exception e){
            log.error("get farm ware house type error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.farmWareHouseType.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWareHouseDto>> queryDoctorWarehouseDto(Long farmId, Integer type, Integer pageNo, Integer pageSize) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            Map<String,Object> params = Maps.newHashMap();
            params.put("farmId", farmId);
            params.put("type", type);
            Paging<DoctorWareHouse> doctorWareHousePaging = doctorWareHouseDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), params);

            // validate null
            if(doctorWareHousePaging.isEmpty()){
                return Response.ok(Paging.empty());
            }

            List<DoctorWareHouse>  wareHouses = doctorWareHousePaging.getData();
            List<DoctorWareHouseTrack> doctorWareHouseTracks = doctorWareHouseTrackDao.queryByWareHouseId(wareHouses.stream().map(m->m.getId()).collect(Collectors.toList()));

            //convert result
            Map<Long,DoctorWareHouseTrack> trackMap = doctorWareHouseTracks.stream().collect(Collectors.toMap(k->k.getWareHouseId(), v->v));

            return Response.ok(new Paging<>(
                    doctorWareHousePaging.getTotal(),
                    wareHouses.stream().map(s->DoctorWareHouseDto.buildWareHouseDto(s, trackMap.get(s.getId()))).collect(Collectors.toList()))
            );
        }catch (Exception e){
            log.error("query warehouse dto error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.wwarehouse.error");
        }
    }

    @Override
    public Response<DoctorWareHouseDto> queryDoctorWareHouseById(@NotNull(message = "input.warehouseId.empty") Long warehouseId) {
        try{
            DoctorWareHouse doctorWareHouse = doctorWareHouseDao.findById(warehouseId);
            checkState(!isNull(doctorWareHouse), "input.warehouseId.error");

            DoctorWareHouseTrack doctorWareHouseTrack = doctorWareHouseTrackDao.findById(warehouseId);

        	return Response.ok(DoctorWareHouseDto.buildWareHouseDto(doctorWareHouse, doctorWareHouseTrack));
        }catch (IllegalStateException se){
            log.warn("illegal state fail, warehouseId:{}, cause:{}", warehouseId,  Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("warehouse info find by id error, warehouseId:{}, cause:{}", warehouseId, Throwables.getStackTraceAsString(e));
            return Response.fail("findBy.warehouseId.fail");
        }
    }
}
