package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMonthlyStock;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-17 15:02:59
 * Created by [ your name ]
 */
public interface DoctorWarehouseMonthlyStockWriteService {

    /**
     * 创建
     * @param doctorWarehouseMonthlyStock
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseMonthlyStock doctorWarehouseMonthlyStock);

    /**
     * 更新
     * @param doctorWarehouseMonthlyStock
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseMonthlyStock doctorWarehouseMonthlyStock);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}