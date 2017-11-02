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
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseStock>
     */
    Response<List<DoctorWarehouseStock>> list(DoctorWarehouseStock criteria);

    Response<List<DoctorWarehouseStock>> list(Map<String, Object> criteria);

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