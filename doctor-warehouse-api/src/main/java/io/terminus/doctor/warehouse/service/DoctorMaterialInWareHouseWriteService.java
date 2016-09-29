package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;

import javax.validation.constraints.NotNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorMaterialInWareHouseWriteService {

    Response<Boolean> create(DoctorMaterialInWareHouse materialInWareHouse);
    /**
     * 录入用户消耗信息
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    Response<Long> consumeMaterialInfo(@NotNull(message = "input.dto.empty") DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto);

    /**
     * 用户录入生产数量信息
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    Response<Long> providerMaterialInfo(@NotNull(message = "input.dto.empty") DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto);

    /**
     * 仓库间物资转移, 即调拨
     * @return
     */
    Response moveMaterial(DoctorMaterialConsumeProviderDto diaochuDto, DoctorMaterialConsumeProviderDto diaoruDto);

    /**
     * 删除对应的仓库中某种物料信息
     * @param materialInWareHouseId
     * @param userId
     * @param userName
     * @return
     */
    Response<Boolean> deleteMaterialInWareHouseInfo(@NotNull(message = "input.materialInWareHouseId.empty") Long materialInWareHouseId,
                                                    @NotNull(message = "input.userId.empty") Long userId, String userName);

    /**
     * 仓库事件回滚
     * @param eventId
     * @return
     */
    Response<Boolean> rollback(Long eventId);
}
