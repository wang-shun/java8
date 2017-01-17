package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.DoctorWareHouseDto;
import io.terminus.doctor.basic.dto.WarehouseEventReport;
import io.terminus.doctor.basic.model.DoctorFarmWareHouseType;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.DoctorWareHouseTrack;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;
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

    private final DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao;

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public DoctorWareHouseReadServiceImpl(DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao,
                                          DoctorWareHouseDao doctorWareHouseDao,
                                          DoctorWareHouseTrackDao doctorWareHouseTrackDao,
                                          DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao,
                                          DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao){
        this.doctorFarmWareHouseTypeDao = doctorFarmWareHouseTypeDao;
        this.doctorWareHouseDao = doctorWareHouseDao;
        this.doctorWareHouseTrackDao = doctorWareHouseTrackDao;
        this.doctorMaterialPriceInWareHouseDao = doctorMaterialPriceInWareHouseDao;
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
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

            List<DoctorWareHouseDto> doctorWareHouseDtoList = wareHouses.stream().map(s->DoctorWareHouseDto.buildWareHouseDto(s, trackMap.get(s.getId()))).collect(Collectors.toList());
            if(!doctorWareHouseDtoList.isEmpty()){
                getWareHouseMonthInfo(doctorWareHouseDtoList);
            }
            return Response.ok(new Paging<>( doctorWareHousePaging.getTotal(),doctorWareHouseDtoList));
        }catch (Exception e){
            log.error("query warehouse dto error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.wwarehouse.error");
        }
    }

    /**
     * 获取仓库每个月的出入库,调拨信息
     * @param doctorWareHouseDtoList
     */
    private void getWareHouseMonthInfo(List<DoctorWareHouseDto> doctorWareHouseDtoList) {
        doctorWareHouseDtoList.stream().forEach(
                doctorWareHouseDto ->{
                    Map<String, Object> stockMap = doctorMaterialPriceInWareHouseDao.currentStockInfo(doctorWareHouseDto.getFarmId(), doctorWareHouseDto.getWarehouseId(), doctorWareHouseDto.getType());
                    if(stockMap != null && !stockMap.isEmpty()){
                        doctorWareHouseDto.setStockCount(Double.valueOf(Objects.toString(stockMap.get("count"))));
                        doctorWareHouseDto.setStockAmount(Double.valueOf(Objects.toString(stockMap.get("amount"))));
                    }else{
                        doctorWareHouseDto.setStockCount(0D);
                        doctorWareHouseDto.setStockAmount(0D);
                    }
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("farmId", doctorWareHouseDto.getFarmId());
                    param.put("wareHouseId", doctorWareHouseDto.getWarehouseId());
                    param.put("startAt", DateTime.now().withDayOfMonth(1).toString(DateUtil.DATE));
                    param.put("endAt", DateTime.now().withDayOfMonth(1).plusMonths(1).toString(DateUtil.DATE));
                    List<WarehouseEventReport> warehouseEventReportList = doctorMaterialConsumeProviderDao.warehouseEventReport(param);
                    //预先将出入库数量,金额置为0
                    doctorWareHouseDto.setMonthInCount(0D);
                    doctorWareHouseDto.setMonthInAmount(0D);
                    doctorWareHouseDto.setMonthOutCount(0D);
                    doctorWareHouseDto.setMonthOutAmount(0D);
                    doctorWareHouseDto.setMonthTransferInCount(0D);
                    doctorWareHouseDto.setMonthTransferInAmount(0D);
                    doctorWareHouseDto.setMonthTransferOutCount(0D);
                    doctorWareHouseDto.setMonthTransferOutAmount(0D);
                    if( warehouseEventReportList != null && !warehouseEventReportList.isEmpty()) {

                        warehouseEventReportList.stream().forEach(warehouseEventReport -> {
                            switch (DoctorMaterialConsumeProvider.EVENT_TYPE.from(warehouseEventReport.getEventType())) {

                                case PROVIDER:
                                    doctorWareHouseDto.setMonthInCount( doctorWareHouseDto.getMonthInCount() + warehouseEventReport.getCount());
                                    doctorWareHouseDto.setMonthInAmount(doctorWareHouseDto.getMonthInAmount() + warehouseEventReport.getAmount());
                                    break;
                                case CONSUMER:
                                    doctorWareHouseDto.setMonthOutCount( doctorWareHouseDto.getMonthOutCount() + warehouseEventReport.getCount());
                                    doctorWareHouseDto.setMonthOutAmount(doctorWareHouseDto.getMonthOutAmount() + warehouseEventReport.getAmount());
                                    break;
                                case PANKUI:
                                    break;
                                case PANYING:
                                    break;
                                case DIAOCHU:
                                    doctorWareHouseDto.setMonthTransferOutCount( doctorWareHouseDto.getMonthTransferOutCount() + warehouseEventReport.getCount());
                                    doctorWareHouseDto.setMonthTransferOutAmount(doctorWareHouseDto.getMonthTransferOutAmount() + warehouseEventReport.getAmount());
                                    break;
                                case DIAORU:
                                    doctorWareHouseDto.setMonthTransferInCount( doctorWareHouseDto.getMonthTransferInCount() + warehouseEventReport.getCount());
                                    doctorWareHouseDto.setMonthTransferInAmount(doctorWareHouseDto.getMonthTransferInAmount() + warehouseEventReport.getAmount());
                                    break;
                                case FORMULA_RAW_MATERIAL:
                                    doctorWareHouseDto.setMonthOutCount( doctorWareHouseDto.getMonthOutCount() + warehouseEventReport.getCount());
                                    doctorWareHouseDto.setMonthOutAmount(doctorWareHouseDto.getMonthOutAmount() + warehouseEventReport.getAmount());
                                    break;
                                case FORMULA_FEED:
                                    doctorWareHouseDto.setMonthInCount( doctorWareHouseDto.getMonthInCount() + warehouseEventReport.getCount());
                                    doctorWareHouseDto.setMonthInAmount(doctorWareHouseDto.getMonthInAmount() + warehouseEventReport.getAmount());
                                    break;
                            }
                        });

                    }
                    //不是饲料和原料的仓库无法统计数量
                    if(!doctorWareHouseDto.getType().equals(WareHouseType.FEED.getKey()) && !doctorWareHouseDto.getType().equals(WareHouseType.MATERIAL.getKey())){
                        doctorWareHouseDto.setStockCount(null);
                        doctorWareHouseDto.setMonthInCount(null);
                        doctorWareHouseDto.setMonthOutCount(null);
                        doctorWareHouseDto.setMonthTransferInCount(null);
                        doctorWareHouseDto.setMonthTransferOutCount(null);
                    }

                }
        );
    }

    @Override
    public Response<List<DoctorWareHouseDto>> listDoctorWareHouseDto(Long farmId, Integer type, String warehouseName){
        try{
            List<DoctorWareHouse> wareHouses = doctorWareHouseDao.findByFarmId(farmId).stream()
                    .filter(wareHouse -> type == null || Objects.equals(type, wareHouse.getType()))
                    .filter(wareHouse -> warehouseName == null || wareHouse.getWareHouseName().contains(warehouseName))
                    .collect(Collectors.toList());
            if(wareHouses.isEmpty()){
                return Response.ok(Collections.emptyList());
            }
            List<DoctorWareHouseTrack> doctorWareHouseTracks = doctorWareHouseTrackDao.queryByWareHouseId(wareHouses.stream().map(DoctorWareHouse::getId).collect(Collectors.toList()));
            Map<Long,DoctorWareHouseTrack> trackMap = doctorWareHouseTracks.stream().collect(Collectors.toMap(DoctorWareHouseTrack::getWareHouseId, v->v));
            return Response.ok(wareHouses.stream().map(s->DoctorWareHouseDto.buildWareHouseDto(s, trackMap.get(s.getId()))).collect(Collectors.toList()));
        }catch(Exception e){
            log.error("list DoctorWareHouseDto fail, cause : {}", Throwables.getStackTraceAsString(e));
            return Response.fail("list.wwarehouse.error");
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

    @Override
    public Response<DoctorWareHouse> findById(Long warehouseId){
        try{
            return Response.ok(doctorWareHouseDao.findById(warehouseId));
        }catch(Exception e){
            log.error("warehouse info find by id error, warehouseId:{}, cause:{}", warehouseId, Throwables.getStackTraceAsString(e));
            return Response.fail("findBy.warehouseId.fail");
        }
    }
}
