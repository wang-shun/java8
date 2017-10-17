package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-13 13:14:30
 * Created by [ your name ]
 */
public interface DoctorWarehouseSkuReadService {

    /**
     * 查询
     * @param id
     * @return doctorWarehouseSku
     */
    Response<DoctorWarehouseSku> findById(Long id);

    Response<List<DoctorWarehouseSku>> findByIds(List<Long> ids);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseSku>
     */
    Response<Paging<DoctorWarehouseSku>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     * @param criteria
     * @return List<DoctorWarehouseSku>
     */
    Response<List<DoctorWarehouseSku>> list(Map<String, Object> criteria);
}