package io.terminus.doctor.basic.service;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStock;

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


    Response<Paging<DoctorWarehouseStock>> paging(Integer pageNo, Integer pageSize, DoctorWarehouseStock criteria);


    Response<Paging<DoctorWarehouseStock>> pagingMergeVendor(Integer pageNo, Integer pageSize, DoctorWarehouseStock criteria);

    Response<Paging<DoctorWarehouseStock>> pagingMergeVendor(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseStock>
     */
    Response<List<DoctorWarehouseStock>> list(DoctorWarehouseStock criteria);

    Response<List<DoctorWarehouseStock>> listMergeVendor(DoctorWarehouseStock criteria);

    /**
     * 获取农场下所有仓库中指定物料的库存
     *
     * @param farmID
     * @param materialID
     * @return
     */
    Response<List<DoctorWarehouseStock>> list(Long farmID, Long materialID);


    Response<DoctorWarehouseStock> findOneByCriteria(DoctorWarehouseStock criteria);


    Response<Boolean> existed(DoctorWarehouseStock criteria);
}