package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.manager.MaterialInWareHouseManager;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorMaterialInWareHouseWriteServiceImpl(MaterialInWareHouseManager materialInWareHouseManager){
        this.materialInWareHouseManager = materialInWareHouseManager;
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
    public Response moveMaterial(DoctorMaterialConsumeProviderDto diaochuDto, DoctorMaterialConsumeProviderDto diaoruDto){
        try{
            materialInWareHouseManager.moveMaterial(diaochuDto, diaoruDto);
            return Response.ok();
        }catch(RuntimeException e){
            return Response.fail(e.getMessage());
        }catch(Exception e){
            log.error("move material fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("move.material.fail");
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
        }catch (ServiceException e){
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("rollback warehouse event fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("rollback.warehouse.event.fail");
        }
    }
}
