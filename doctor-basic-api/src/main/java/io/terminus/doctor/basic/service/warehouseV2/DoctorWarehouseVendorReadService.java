package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 15:57:14
 * Created by [ your name ]
 */
public interface DoctorWarehouseVendorReadService {

    /**
     * 查询
     * @param id
     * @return doctorWarehouseVendor
     */
    Response<DoctorWarehouseVendor> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseVendor>
     */
    Response<Paging<DoctorWarehouseVendor>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorWarehouseVendor>
    */
    Response<List<DoctorWarehouseVendor>> list(Map<String, Object> criteria);
}