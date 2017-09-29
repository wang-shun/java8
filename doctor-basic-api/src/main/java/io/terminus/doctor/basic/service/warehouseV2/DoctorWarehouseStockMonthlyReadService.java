package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-29 13:22:37
 * Created by [ your name ]
 */
public interface DoctorWarehouseStockMonthlyReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehouseStockMonthly
     */
    Response<DoctorWarehouseStockMonthly> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseStockMonthly>
     */
    Response<Paging<DoctorWarehouseStockMonthly>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseStockMonthly>
     */
    Response<List<DoctorWarehouseStockMonthly>> list(Map<String, Object> criteria);

    Response<DoctorWarehouseStockMonthly> findByWarehouseAndMaterial(Long warehouseId, Long materialId);

    Response<AmountAndQuantityDto> countWarehouseBalance(Long warehouseId, int handleYear, int handleMonth);
}