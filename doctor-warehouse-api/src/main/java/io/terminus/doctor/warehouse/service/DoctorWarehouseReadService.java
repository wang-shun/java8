package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;

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
     * 获取猪场不同原料类型的仓库统计信息
     * @param farmId
     * @return
     */
    Response<List<DoctorFarmWareHouseType>> queryDoctorFarmWareHouseType(@NotNull(message = "input.farmId.empty") String farmId);

//    /**
//     *
//     * @param farmId
//     * @return
//     */
//    Response<List<DoctorWareHouseDto>> queryWarehouseByFarmId(@NotNull(message = "input.farmId.empty") String farmId);
}
