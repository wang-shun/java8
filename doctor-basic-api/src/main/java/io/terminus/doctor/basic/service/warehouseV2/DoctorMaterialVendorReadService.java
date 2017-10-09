package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.model.warehouseV2.DoctorMaterialVendor;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-11 18:34:04
 * Created by [ your name ]
 */
public interface DoctorMaterialVendorReadService {

    /**
     * 查询
     * @param id
     * @return doctorMaterialVendor
     */
    Response<DoctorMaterialVendor> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorMaterialVendor>
     */
    Response<Paging<DoctorMaterialVendor>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorMaterialVendor>
    */
    Response<List<DoctorMaterialVendor>> list(Map<String, Object> criteria);

    Response<List<DoctorMaterialVendor>> list(DoctorMaterialVendor criteria);
}