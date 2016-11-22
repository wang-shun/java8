package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dto.BarnConsumeMaterialReport;
import io.terminus.doctor.warehouse.dto.MaterialCountAmount;
import io.terminus.doctor.warehouse.dto.MaterialEventReport;
import io.terminus.doctor.warehouse.dto.WarehouseEventReport;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RpcProvider
@Slf4j
public class DoctorMaterialConsumeProviderReadServiceImpl implements DoctorMaterialConsumeProviderReadService {

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public DoctorMaterialConsumeProviderReadServiceImpl(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao) {
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    @Override
    public Response<DoctorMaterialConsumeProvider> findById(Long id){
        try{
            return Response.ok(doctorMaterialConsumeProviderDao.findById(id));
        }catch(Exception e){
            log.error("find DoctorMaterialConsumeProvider fail, id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("find.DoctorMaterialConsumeProvider.fail");
        }
    }

    @Override
    public Response<Paging<DoctorMaterialConsumeProvider>> page(Long farmId, Long warehouseId, Long materialId, Integer eventType, Integer materilaType,
                                                              Long staffId, String startAt, String endAt, Integer pageNo, Integer size) {
        try{
            DoctorMaterialConsumeProvider model = DoctorMaterialConsumeProvider.builder()
                    .wareHouseId(warehouseId).materialId(materialId).eventType(eventType).type(materilaType)
                    .farmId(farmId)
                    .staffId(staffId).build();
            Map<String, Object> map = BeanMapper.convertObjectToMap(model);
            map.put("startAt", startAt);
            map.put("endAt", endAt);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            return Response.ok(doctorMaterialConsumeProviderDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), Params.filterNullOrEmpty(map)));
        }catch(Exception e){
            log.error("page DoctorMaterialConsumeProvider failed, cause :{}", Throwables.getStackTraceAsString(e));
            return Response.fail("page.DoctorMaterialConsumeProvider.fail");
        }
    }

    @Override
    public Response<List<DoctorMaterialConsumeProvider>> list(Long farmId, Long warehouseId, Long materialId, String materialName,
                                                              Integer eventType, List<Integer> eventTypes, Integer materilaType,
                                                              Long staffId, String startAt, String endAt) {
        try{
            DoctorMaterialConsumeProvider model = DoctorMaterialConsumeProvider.builder()
                    .wareHouseId(warehouseId).materialId(materialId).eventType(eventType).type(materilaType)
                    .farmId(farmId).materialName(Params.trimToNull(materialName))
                    .staffId(staffId).build();
            Map<String, Object> map = BeanMapper.convertObjectToMap(model);
            map.put("startAt", startAt);
            map.put("endAt", endAt);
            if(eventTypes != null && !eventTypes.isEmpty()){
                map.put("eventTypes", eventTypes);
            }
            return Response.ok(doctorMaterialConsumeProviderDao.list(Params.filterNullOrEmpty(map)));
        }catch(Exception e){
            log.error("page DoctorMaterialConsumeProvider failed, cause :{}", Throwables.getStackTraceAsString(e));
            return Response.fail("page.DoctorMaterialConsumeProvider.fail");
        }
    }

    @Override
    public Response<Paging<MaterialCountAmount>> countAmount(Long farmId, Long warehouseId, Long materialId, Integer eventType, Integer materilaType,
                                                     Long barnId, Long groupId, Long staffId, String startAt, String endAt, Integer pageNo, Integer size){
        try{
            DoctorMaterialConsumeProvider model = DoctorMaterialConsumeProvider.builder()
                    .wareHouseId(warehouseId).materialId(materialId).eventType(eventType).type(materilaType)
                    .farmId(farmId).barnId(barnId).groupId(groupId)
                    .staffId(staffId)
                    .build();
            Map<String, Object> map = BeanMapper.convertObjectToMap(model);
            map.put("startAt", startAt);
            map.put("endAt", endAt);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            return Response.ok(doctorMaterialConsumeProviderDao.countAmount(pageInfo.getOffset(), pageInfo.getLimit(), Params.filterNullOrEmpty(map)));
        }catch(Exception e){
            log.error("MaterialCountAmount failed, cause :{}", Throwables.getStackTraceAsString(e));
            return Response.fail("material.count.amount.fail");
        }
    }

    @Override
    public Response<Double> sumConsumeFeed(Long farmId, Long wareHouseId, Long materialId, Long staffId, Long barnId, Long groupId,
                                           String startAt, String endAt){
        try{
            DoctorMaterialConsumeProvider model = DoctorMaterialConsumeProvider.builder()
                    .materialId(materialId).wareHouseId(wareHouseId).barnId(barnId).groupId(groupId)
                    .staffId(staffId).farmId(farmId)
                    .build();
            Map<String, Object> map = BeanMapper.convertObjectToMap(model);
            map.put("startAt", startAt);
            map.put("endAt", endAt);
            return Response.ok(doctorMaterialConsumeProviderDao.sumConsumeFeed(Params.filterNullOrEmpty(map)));
        }catch (Exception e){
            log.error("sumConsume failed, farmId={}, wareHouseId={}, materialId={}, staffId={}, barnId={}, groupId={}, startAt={}, endAt={}, cause :{}",
                    farmId, wareHouseId, materialId, staffId, barnId, groupId, startAt, endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("sum.consume.fail");
        }
    }

    @Override
    public Response<List<WarehouseEventReport>> warehouseEventReport(Long farmId, Long warehouseId, Long materialId, String materialName,
                                                                     Integer eventType, List<Integer> eventTypes, Integer materilaType,
                                                                     Long staffId, String startAt, String endAt) {
        try{
            DoctorMaterialConsumeProvider model = DoctorMaterialConsumeProvider.builder()
                    .wareHouseId(warehouseId).materialId(materialId).eventType(eventType).type(materilaType)
                    .farmId(farmId).materialName(Params.trimToNull(materialName))
                    .staffId(staffId).build();
            Map<String, Object> map = BeanMapper.convertObjectToMap(model);
            map.put("startAt", startAt);
            map.put("endAt", endAt);
            if(eventTypes != null && !eventTypes.isEmpty()){
                map.put("eventTypes", eventTypes);
            }
            return Response.ok(doctorMaterialConsumeProviderDao.warehouseEventReport(Params.filterNullOrEmpty(map)));
        }catch(Exception e){
            log.error("warehouseEventReport failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("warehouseEventReport.fail");
        }
    }

    @Override
    public Response<List<MaterialEventReport>> materialEventReport(Long farmId, Long warehouseId, WareHouseType type, Date startAt, Date endAt) {
        try{
            return Response.ok(doctorMaterialConsumeProviderDao.materialEventReport(farmId, warehouseId, type, startAt, endAt));
        }catch(Exception e){
            log.error("warehouseEventReport failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("warehouseEventReport.fail");
        }
    }

    @Override
    public Response<Paging<BarnConsumeMaterialReport>> barnConsumeMaterialReport(Long farmId, Long wareHouseId, Long materialId, String materialName,
                                                                               WareHouseType type, Long barnId, Long staffId, Long creatorId,
                                                                               String startAt, String endAt, Integer pageNo, Integer pageSize) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorMaterialConsumeProviderDao.barnReport(
                    farmId, wareHouseId, materialId, materialName, type, barnId, staffId, creatorId, startAt, endAt, pageInfo.getOffset(), pageInfo.getLimit())
            );
        }catch(Exception e){
            log.error("barnConsumeMaterialReport failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("barnConsumeMaterialReport.fail");
        }
    }

}
