package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-18 09:42:25
 * Created by [ your name ]
 */
public interface DoctorWarehouseStockReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehouseStock
     */
    Response<DoctorWarehouseStock> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseStock>
     */
    Response<Paging<DoctorWarehouseStock>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);


    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehouseStock>> paging(Integer pageNo, Integer pageSize, DoctorWarehouseStock criteria);


    /**
     * 分页
     * 同一个仓库下的同一个物料，合并不同供应商
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehouseStock>> pagingMergeVendor(Integer pageNo, Integer pageSize, DoctorWarehouseStock criteria);

    /**
     * 分页
     * 对同一个仓库下的同一个物料合并不同供应商
     * 支持materialNameLike参数，对materialName模糊查询
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehouseStock>> pagingMergeVendor(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseStock>
     */
    Response<List<DoctorWarehouseStock>> list(DoctorWarehouseStock criteria);

    /**
     * 列表
     * 对同一个仓库下的同一物料合并不同供应商
     *
     * @param criteria
     * @return
     */
    Response<List<DoctorWarehouseStock>> listMergeVendor(DoctorWarehouseStock criteria);

    /**
     * 获取农场下所有仓库中指定物料的库存
     *
     * @param farmID
     * @param materialID
     * @return
     */
    Response<List<DoctorWarehouseStock>> list(Long farmID, Long materialID);


    /**
     * 查询单个库存，没有返回Null
     *
     * @param criteria
     * @return
     */
    Response<DoctorWarehouseStock> findOneByCriteria(DoctorWarehouseStock criteria);

    /**
     * 库存是否已存在
     *
     * @param criteria
     * @return
     */
    Response<Boolean> existed(DoctorWarehouseStock criteria);





}