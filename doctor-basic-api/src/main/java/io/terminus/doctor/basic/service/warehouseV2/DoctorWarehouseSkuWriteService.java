package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-13 13:14:30
 * Created by [ your name ]
 */
public interface DoctorWarehouseSkuWriteService {

    /**
     * 创建
     *
     * @param doctorWarehouseSku
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseSku doctorWarehouseSku);

    /**
     * 更新
     *
     * @param doctorWarehouseSku
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseSku doctorWarehouseSku,String vendorName);

    /**
     * 删除
     *
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);


    /**
     * 生成物料编码
     *
     * @param orgId
     * @param type
     * @return
     */
    Response<String> generateCode(Long orgId, Integer type);
}