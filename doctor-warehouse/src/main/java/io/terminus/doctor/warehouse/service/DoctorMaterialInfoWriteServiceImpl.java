package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.warehouse.constants.DoctorMaterialInfoConstants;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.manager.MaterialInWareHouseManager;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-23
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
public class DoctorMaterialInfoWriteServiceImpl implements DoctorMaterialInfoWriteService{

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    private final DoctorWareHouseDao doctorWareHouseDao;

    private final MaterialInWareHouseManager materialInWareHouseManager;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    private DoctorMaterialInfoWriteServiceImpl(DoctorMaterialInfoDao doctorMaterialInfoDao,
                                               DoctorWareHouseDao doctorWareHouseDao,
                                               MaterialInWareHouseManager materialInWareHouseManager){
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
        this.doctorWareHouseDao = doctorWareHouseDao;
        this.materialInWareHouseManager = materialInWareHouseManager;
    }


    @Override
    public Response<Long> createMaterialInfo(DoctorMaterialInfo doctorMaterialInfo) {
        try{
            checkState(doctorMaterialInfoDao.create(doctorMaterialInfo), "create.materialInfo.fail");
            publishMaterialInfo(DataEventType.MaterialInfoCreateEvent.getKey(), ImmutableMap.of("materialInfoCreatedId", doctorMaterialInfo.getId()));
            return Response.ok(doctorMaterialInfo.getId());
        }catch (Exception e){
            log.error("create material info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.materialInfo.fail");
        }
    }

    @Override
    public Response<Boolean> createMaterialProductRatioInfo(DoctorMaterialProductRatioDto doctorMaterialProductRatioDto) {
        try{
            // 校验物料信息存在
            DoctorMaterialInfo doctorMaterialInfo = doctorMaterialInfoDao.findById(doctorMaterialProductRatioDto.getMaterialId());
            checkState(!isNull(doctorMaterialInfo),"doctor.materialRatio.error");

            // 选择物料信息不校验， 可能物料信息删除等， 等待生产的时候校验对应的物料信息
            // calculate percent
            DoctorMaterialInfo.MaterialProduce materialProduce = doctorMaterialProductRatioDto.getProduce();
            Long total = materialProduce.calculateTotalPercent();
            checkState(Objects.equals(total, DoctorMaterialInfo.DEFAULT_COUNT), "input.totalMaterialCount.error");

            doctorMaterialInfo.setExtraMap(ImmutableMap.of(DoctorMaterialInfoConstants.MATERIAL_PRODUCE,
                    JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialProductRatioDto.getProduce())));
            doctorMaterialInfoDao.update(doctorMaterialInfo);
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("create material product ratio fail,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.materialProduct.fail");
        }
    }

    @Override
    public Response<DoctorMaterialInfo.MaterialProduce> produceMaterial(Long materialId, Long produceCount) {
        try{
            // get material
            DoctorMaterialInfo doctorMaterialInfo = doctorMaterialInfoDao.findById(materialId);
            if(isNull(doctorMaterialInfo)){
                return Response.fail("input.materialId.error");
            }

            // 校验对应的原料生产信息
            if(!doctorMaterialInfo.getExtraMap().containsKey(DoctorMaterialInfoConstants.MATERIAL_PRODUCE)){
                return Response.fail("material.percentRatio.error");
            }

            DoctorMaterialInfo.MaterialProduce materialProduce = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(
                    doctorMaterialInfo.getExtraMap().get(DoctorMaterialInfoConstants.MATERIAL_PRODUCE), DoctorMaterialInfo.MaterialProduce.class);

            // count ration
            checkState(materialProduce.calculatePercentByTotal(produceCount), "material.produce.error");
            return Response.ok(materialProduce);
        }catch (Exception e){
            log.error("material produce error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("produce.material.fail");
        }
    }

    @Override
    public Response<Boolean> realProduceMaterial(DoctorWareHouseBasicDto doctorWareHouseBasicDto, DoctorMaterialInfo.MaterialProduce materialProduce) {
        try{
            // validate
            // 校验Material
            DoctorMaterialInfo doctorMaterialInfo = doctorMaterialInfoDao.findById(doctorWareHouseBasicDto.getMaterialId());
            checkState(!isNull(doctorMaterialInfo), "input.materialId.error");

            DoctorWareHouse doctorWareHouse = this.doctorWareHouseDao.findById(doctorWareHouseBasicDto.getWareHouseId());
            checkState(!isNull(doctorMaterialInfo), "input.warehouseId.error");

            // 校验用户修改数量信息
            validateCountRange(materialProduce);

            // 校验仓库
            checkState(Objects.equals(doctorWareHouse.getType(), doctorMaterialInfo.getType()), "produce.targetWarehouseType.fail");

            // produce 生产对应的数据
            checkState(materialInWareHouseManager.produceMaterialInfo(doctorWareHouseBasicDto, doctorWareHouse, doctorMaterialInfo, materialProduce),
                    "produce.materialInfo.error");
            return Response.ok(Boolean.TRUE);
        }catch(IllegalStateException e){
            log.error("real material produce fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e){
            log.error("real material produce fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("produce.realMaterial.fail");
        }
    }

    /**
     * validate produce material %5 dis
     * @param materialProduce
     * @return
     */
    private void validateCountRange(DoctorMaterialInfo.MaterialProduce materialProduce){
        Long realTotal = materialProduce.getMaterialProduceEntries().stream()
                .map(DoctorMaterialInfo.MaterialProduceEntry::getMaterialCount)
                .reduce((a,b)->a+b).orElse(0l);
        checkState(!Objects.equals(0l, realTotal), "input.materialProduceTotal.error");
        double dis = (realTotal-materialProduce.getTotal()) * 100d / materialProduce.getTotal();
        checkState(Math.abs(dis)<=5, "produce.materialCountChange.error");
    }

    private <T> void publishMaterialInfo(Integer eventType, T data){
        if(!Objects.isNull(publisher)){
           try{
               publisher.publish(DataEvent.toBytes(eventType, data));
           }catch (Exception e){
               log.error("material info publisher error, eventType:{} data:{} cause:{}", eventType, data, Throwables.getStackTraceAsString(e));
           }
        }
    }
}
