package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 14:49:19
 * Created by [ your name ]
 */
public interface DoctorWarehouseStockHandleWriteService {

    /**
     * 创建
     * @param doctorWarehouseStockHandle
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseStockHandle doctorWarehouseStockHandle);

    /**
     * 更新
     * @param doctorWarehouseStockHandle
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseStockHandle doctorWarehouseStockHandle);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<String> delete(Long id);

}