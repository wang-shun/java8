package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseItemOrg;

import io.terminus.common.model.Response;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-11-02 22:15:38
 * Created by [ your name ]
 */
public interface DoctorWarehouseItemOrgWriteService {

    /**
     * 创建
     *
     * @param doctorWarehouseItemOrg
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseItemOrg doctorWarehouseItemOrg);

    /**
     * 更新
     *
     * @param doctorWarehouseItemOrg
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseItemOrg doctorWarehouseItemOrg);

    /**
     * 删除
     *
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

    Response<Boolean> boundToOrg(String itemIds, Long orgId);

}