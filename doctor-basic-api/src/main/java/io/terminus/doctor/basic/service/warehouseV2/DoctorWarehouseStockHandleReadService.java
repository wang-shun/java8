package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 14:49:19
 * Created by [ your name ]
 */
public interface DoctorWarehouseStockHandleReadService {

    /**
     *得到单位名称
     * @param relStockHandleId
     * @return
     */
    Response<String> getNameByUnit(Long relStockHandleId);

    /**
     * 得到配方入库仓库
     * @param RelId
     * @return
     */
    Response<String> findwarehouseName(Long RelId);

    /**
     * 查询
     * @param id
     * @return doctorWarehouseStockHandle
     */
    Response<DoctorWarehouseStockHandle> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseStockHandle>
     */
    Response<Paging<DoctorWarehouseStockHandle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorWarehouseStockHandle>
    */
    Response<List<DoctorWarehouseStockHandle>> list(Map<String, Object> criteria);
}