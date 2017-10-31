package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;

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
     *
     * @param id
     * @return doctorWarehouseMaterialHandle
     */
    Response<DoctorWarehouseMaterialHandle> findById(Long id);


    /**
     * 根据单据查询操作明细
     *
     * @param stockHandleId
     * @return
     */
    Response<List<DoctorWarehouseMaterialHandle>> findByStockHandle(Long stockHandleId);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseMaterialHandle>
     */
    Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);


    /**
     * 高级分页
     * 添加对handleDate开始和结束日期的过滤；type类型的多条件过滤
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehouseMaterialHandle>> advPaging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);


    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, DoctorWarehouseMaterialHandle criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseMaterialHandle>
     */
    Response<List<DoctorWarehouseMaterialHandle>> list(Map<String, Object> criteria);


    /**
     * 高级列表
     * 添加对handleDate开始和结束日期的过滤；type类型的多条件过滤
     *
     * @param criteria
     * @return
     */
    Response<List<DoctorWarehouseMaterialHandle>> advList(Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return
     */
    Response<List<DoctorWarehouseMaterialHandle>> list(DoctorWarehouseMaterialHandle criteria);

    /**
     * 统计仓库纬度（出库，入库，调拨，盘点）金额
     *
     * @param data
     * @return
     */
    Response<Map<Long/*warehouseId*/, Long>> countWarehouseAmount(List<DoctorWarehouseMaterialHandle> data);


    /**
     * 统计仓库纬度（出库，入库，调拨，盘点）金额
     *
     * @param criteria
     * @param types
     * @return
     */
    Response<Map<WarehouseMaterialHandleType, Map<Long, Long>>> countWarehouseAmount(DoctorWarehouseMaterialHandle criteria, WarehouseMaterialHandleType... types);


}