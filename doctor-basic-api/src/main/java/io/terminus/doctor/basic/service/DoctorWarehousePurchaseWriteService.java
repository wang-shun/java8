package io.terminus.doctor.basic.service;


import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehousePurchase;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 00:18:50
 * Created by [ your name ]
 */
public interface DoctorWarehousePurchaseWriteService {

    /**
     * 创建
     * @param doctorWarehousePurchase
     * @return Boolean
     */
    Response<Long> create(DoctorWarehousePurchase doctorWarehousePurchase);

    /**
     * 更新
     * @param doctorWarehousePurchase
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehousePurchase doctorWarehousePurchase);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}