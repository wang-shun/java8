package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 08:56:13
 * Created by [ your name ]
 */
public interface DoctorWarehouseMaterialHandleReadService {

    /**
     * 查询
     * @param id
     * @return doctorWarehouseMaterialHandle
     */
    Response<DoctorWarehouseMaterialHandle> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseMaterialHandle>
     */
    Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorWarehouseMaterialHandle>
    */
    Response<List<DoctorWarehouseMaterialHandle>> list(Map<String, Object> criteria);


    Response<List<DoctorWarehouseMaterialHandle>> list(DoctorWarehouseMaterialHandle criteria);
}