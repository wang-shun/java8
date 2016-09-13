package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorWareHouseReadService {

    /**
     * 获取猪场不同原料类型的仓库统计信息
     * @param farmId
     * @return
     */
    Response<List<DoctorFarmWareHouseType>> queryDoctorFarmWareHouseType(@NotNull(message = "input.farmId.empty") Long farmId);

    /**
     * 仓库分页
     * @param farmId
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorWareHouseDto>> queryDoctorWarehouseDto(@NotNull(message = "input.farmId.empty") Long farmId,
                                                                 Integer type,
                                                                 Integer pageNo,Integer pageSize);

    Response<List<DoctorWareHouseDto>> listDoctorWareHouseDto(@NotNull Long farmId, Integer type, String warehouseName);

    /**
     * 通过WareHouseId 获取仓库信息
     * @param warehouseId
     * @return
     */
    Response<DoctorWareHouseDto> queryDoctorWareHouseById(@NotNull(message = "input.warehouseId.empty") Long warehouseId);

    /**
     * 按id查询仓库
     * @param warehouseId 仓库id
     * @return
     */
    Response<DoctorWareHouse> findById(Long warehouseId);
}
