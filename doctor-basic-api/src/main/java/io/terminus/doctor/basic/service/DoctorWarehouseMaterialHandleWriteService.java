package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 08:56:13
 * Created by [ your name ]
 */
public interface DoctorWarehouseMaterialHandleWriteService {

    /**
     * 创建
     * @param doctorWarehouseMaterialHandle
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle);

    /**
     * 更新
     * @param doctorWarehouseMaterialHandle
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}