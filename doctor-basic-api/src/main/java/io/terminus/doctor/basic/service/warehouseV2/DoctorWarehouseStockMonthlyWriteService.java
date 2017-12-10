package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-29 13:22:37
 * Created by [ your name ]
 */
public interface DoctorWarehouseStockMonthlyWriteService {

    /**
     * 创建
     * @param doctorWarehouseStockMonthly
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseStockMonthly doctorWarehouseStockMonthly);

    /**
     * 更新
     * @param doctorWarehouseStockMonthly
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseStockMonthly doctorWarehouseStockMonthly);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}