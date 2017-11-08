package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseUnitOrg;

import io.terminus.common.model.Response;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-30 16:08:20
 * Created by [ your name ]
 */
public interface DoctorWarehouseUnitOrgWriteService {

    /**
     * 创建
     *
     * @param doctorWarehouseUnitOrg
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseUnitOrg doctorWarehouseUnitOrg);

    /**
     * 更新
     *
     * @param doctorWarehouseUnitOrg
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseUnitOrg doctorWarehouseUnitOrg);

    /**
     * 删除
     *
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);


    Response<Boolean> boundToOrg(Long orgId, String unitIds);
}