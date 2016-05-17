package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorFarmWareHouseTypeDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorWarehouseReadService {

    /**
     * 猪场Id 仓库种类
     * @param farmId
     * @return
     */
    Response<List<DoctorFarmWareHouseTypeDto>> queryFarmWareHouseTypeDto(@NotNull(message = "input.farmId.empty") String farmId);

    /**
     *
     * @param farmId
     * @return
     */
    Response<List<DoctorWareHouseDto>> queryWarehouseByFarmId(@NotNull(message = "input.farmId.empty") String farmId);
}
