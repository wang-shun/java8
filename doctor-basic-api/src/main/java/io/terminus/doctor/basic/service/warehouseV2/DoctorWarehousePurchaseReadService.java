package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 00:18:50
 * Created by [ your name ]
 */
@Deprecated
public interface DoctorWarehousePurchaseReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehousePurchase
     */
    Response<DoctorWarehousePurchase> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehousePurchase>
     */
    Response<Paging<DoctorWarehousePurchase>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);


    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehousePurchase>> paging(Integer pageNo, Integer pageSize, DoctorWarehousePurchase criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehousePurchase>
     */
    Response<List<DoctorWarehousePurchase>> list(Map<String, Object> criteria);


    /**
     * 列表
     *
     * @param criteria
     * @return
     */
    Response<List<DoctorWarehousePurchase>> list(DoctorWarehousePurchase criteria);

    /**
     * 统计猪厂下每个仓库余额
     *
     * @return
     */
    Response<Map<Long, Long>> countWarehouseBalanceAmount(Long farmId);

    /**
     * 计算单价
     *
     * @param warehouseId
     * @param materialId
     * @return
     */
    Response<Long> calculateUnitPrice(Long warehouseId, Long materialId);
}