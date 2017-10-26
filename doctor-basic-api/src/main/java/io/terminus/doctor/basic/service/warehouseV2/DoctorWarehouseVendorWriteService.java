package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;

import io.terminus.common.model.Response;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 15:57:14
 * Created by [ your name ]
 */
public interface DoctorWarehouseVendorWriteService {

    /**
     * 创建
     *
     * @param doctorWarehouseVendor
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseVendor doctorWarehouseVendor);

    /**
     * 更新
     *
     * @param doctorWarehouseVendor
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseVendor doctorWarehouseVendor);

    /**
     * 删除
     *
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);


    /**
     * 将厂家绑定到公司
     *
     * @param vendorId
     * @param orgId
     * @return
     */
    Response<Boolean> boundToOrg(Long vendorId, Long orgId);

}