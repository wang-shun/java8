package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMonthlyStock;

import java.util.List;
import java.util.Map;


/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-17 15:02:59
 * Created by [ your name ]
 */
public interface DoctorWarehouseMonthlyStockReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehouseMonthlyStock
     */
    Response<DoctorWarehouseMonthlyStock> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseMonthlyStock>
     */
    Response<Paging<DoctorWarehouseMonthlyStock>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseMonthlyStock>
     */
    Response<List<DoctorWarehouseMonthlyStock>> list(Map<String, Object> criteria);


//    Response<DoctorWarehouseMonthlyStock> findLastMonth(Map<String, Object> criteria);


}