package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;

import javax.validation.constraints.NotNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 公司原料信息修改
 */
public interface DoctorMaterialInfoWriteService {

    /**
     * 录入生产物料的配比信息
     * @param doctorMaterialProductRatioDto
     * @return
     */
    Response<Boolean> createMaterialProductRatioInfo(
            @NotNull(message = "input.doctorMaterialInfo.empty") DoctorMaterialInfo doctorMaterialInfo,
            @NotNull(message = "input.materialProduct.empty") DoctorMaterialProductRatioDto doctorMaterialProductRatioDto);

    /**
     * 根据生产的数量， 获取对应的生产的比例
     * @param materialId
     * @param produceCount
     * @return
     */
    Response<DoctorMaterialInfo.MaterialProduce> produceMaterial(@NotNull(message = "input.materialId.empty") Long materialId,
                                                                 @NotNull(message = "input.produceCount.empty") Double produceCount);

    /**
     * 对应的物料生产信息比例
     * @param doctorWareHouseBasicDto
     * @param materialProduce
     * @return
     */
    Response<Boolean> realProduceMaterial(@NotNull(message = "input.wareHouseDto.empty") DoctorWareHouseBasicDto doctorWareHouseBasicDto,
                                          @NotNull(message = "input.materialProduce.empty") DoctorMaterialInfo.MaterialProduce materialProduce);
}
