package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.DoctorMoveMaterialDto;
import io.terminus.doctor.basic.manager.MaterialInWareHouseManager;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RpcProvider
public class DoctorMaterialInWareHouseWriteServiceImpl implements DoctorMaterialInWareHouseWriteService{

    private final MaterialInWareHouseManager materialInWareHouseManager;
    private final DoctorMaterialInWareHouseDao materialInWareHouseDao;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorMaterialInWareHouseWriteServiceImpl(MaterialInWareHouseManager materialInWareHouseManager,
                                                     DoctorMaterialInWareHouseDao materialInWareHouseDao){
        this.materialInWareHouseManager = materialInWareHouseManager;
        this.materialInWareHouseDao = materialInWareHouseDao;
    }

    @Override
    public Response<Boolean> create(DoctorMaterialInWareHouse materialInWareHouse){
        try{
            return Response.ok(materialInWareHouseDao.create(materialInWareHouse));
        }catch(Exception e){
            log.error("create DoctorMaterialInWareHouse failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.DoctorMaterialInWareHouse.fail");
        }
    }

    /**
     * 消耗对应的 Consumer 信息内容
     * @param doctorMaterialConsumeProviderDto (参数)farmId, warehouseId, materialId
     * @return
     */
    @Override
    public Response<Long> consumeMaterialInfo(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto) {
        try{
            Long result = materialInWareHouseManager.consumeMaterial(doctorMaterialConsumeProviderDto);

            // publish vaccination medical event
//            if(Objects.equals(doctorMaterialConsumeProviderDto.getType(), WareHouseType.MEDICINE.getKey())
//                    || Objects.equals(doctorMaterialConsumeProviderDto.getType(), WareHouseType.VACCINATION.getKey())) {
//                Map<String, Object> params = Maps.newHashMap();
//                BeanMapper.copy(doctorMaterialConsumeProviderDto, params);
//                publishEvent(params);
//            }
            return Response.ok(result);
        }catch (IllegalStateException se){
            log.warn("illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("consume material fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("consume.material.fail");
        }
    }

    @Override
    public Response<Boolean> batchConsumeMaterialInfo(List<DoctorMaterialConsumeProviderDto> doctorMaterialConsumeProviderDtoList) {
        try {
            materialInWareHouseManager.batchConsumeMaterial(doctorMaterialConsumeProviderDtoList);
            return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException se){
            log.warn("illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("batch consume material fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("batch.consume.material.fail");
        }
    }

    @Override
    public Response<Long> providerMaterialInfo(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        try{
            return Response.ok(materialInWareHouseManager.providerMaterialInWareHouse(doctorMaterialConsumeProviderDto));
        }catch (IllegalStateException | ServiceException se){
            log.warn("provider illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("provider material info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("provider.materialInfo.fail");
        }
    }

    @Override
    public Response<Boolean> batchProviderMaterialInfo(List<DoctorMaterialConsumeProviderDto> doctorMaterialConsumeProviderDtoList) {
        try{
            materialInWareHouseManager.batchProviderMaterialInWareHouse(doctorMaterialConsumeProviderDtoList);
            return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException | ServiceException se){
            log.warn("batch provider illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("batch provider material info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("batch.provider.materialInfo.fail");
        }
    }

    @Override
    public Response moveMaterial(DoctorMoveMaterialDto dto){
        try{
            materialInWareHouseManager.moveMaterial(dto);
            return Response.ok();
        }catch(RuntimeException e){
            return Response.fail(e.getMessage());
        }catch(Exception e){
            log.error("move material fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("move.material.fail");
        }
    }

    @Override
    public Response<Boolean> batchMoveMaterial(List<DoctorMoveMaterialDto> dtoList) {
        try{
            materialInWareHouseManager.batchMoveMaterial(dtoList);
            return Response.ok(Boolean.TRUE);
        }catch(RuntimeException e){
            return Response.fail(e.getMessage());
        }catch(Exception e){
            log.error("batch move material fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("batch.move.material.fail");
        }
    }

    @Override
    public Response<Boolean> batchInventory(List<DoctorMaterialConsumeProviderDto> dtoList) {
        try {
            materialInWareHouseManager.batchInventory(dtoList);
            return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException | ServiceException se){
            log.warn("batch inventory illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("batch inventory fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("batch.inventory.fail");
        }
    }

    @Override
    public Response<Boolean> deleteMaterialInWareHouseInfo(Long materialInWareHouseId, Long userId, String userName) {
        try{
            return Response.ok(materialInWareHouseManager.deleteMaterialInWareHouse(materialInWareHouseId, userId, userName));
        }catch (Exception e){
            log.error("delete material in warehouse info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("delete.materialInWareHouse.fail");
        }
    }

    @Override
    public Response<Boolean> rollback(Long eventId){
        try{
            materialInWareHouseManager.rollback(eventId);
            return Response.ok(true);
        }catch (RuntimeException e){
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("rollback warehouse event fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("rollback.warehouse.event.fail");
        }
    }
}
