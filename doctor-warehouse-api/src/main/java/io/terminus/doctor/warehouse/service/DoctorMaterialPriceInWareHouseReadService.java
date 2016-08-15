package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialPriceInWareHouse;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 仓库中各物料每次入库的剩余量读服务
 * Date: 2016-08-15
 */

public interface DoctorMaterialPriceInWareHouseReadService {

    /**
     * 根据id查询仓库中各物料每次入库的剩余量
     * @param id 主键id
     * @return 仓库中各物料每次入库的剩余量
     */
    Response<DoctorMaterialPriceInWareHouse> findById(Long id);

    /**
     * 根据i仓库d查询仓库中各物料每次入库的剩余量
     * @param wareHouseId 仓库id
     * @return 仓库中各物料每次入库的剩余量
     */
    Response<List<DoctorMaterialPriceInWareHouse>> findByWareHouseId(Long wareHouseId);

}
