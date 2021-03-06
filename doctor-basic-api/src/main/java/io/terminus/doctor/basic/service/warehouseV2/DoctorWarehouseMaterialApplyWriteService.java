package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
public interface DoctorWarehouseMaterialApplyWriteService {

    /**
     * 创建
     * @param doctorWarehouseMaterialApply
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseMaterialApply doctorWarehouseMaterialApply);

    /**
     * 更新
     * @param doctorWarehouseMaterialApply
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseMaterialApply doctorWarehouseMaterialApply);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}