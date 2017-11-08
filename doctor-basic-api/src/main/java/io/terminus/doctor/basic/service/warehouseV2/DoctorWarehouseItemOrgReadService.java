package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseItemOrg;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-11-02 22:15:38
 * Created by [ your name ]
 */
public interface DoctorWarehouseItemOrgReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehouseItemOrg
     */
    Response<DoctorWarehouseItemOrg> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseItemOrg>
     */
    Response<Paging<DoctorWarehouseItemOrg>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseItemOrg>
     */
    Response<List<DoctorWarehouseItemOrg>> list(Map<String, Object> criteria);


    Response<List<DoctorBasicMaterial>> findByOrgId(Long orgId);

    Response<List<DoctorBasicMaterial>> suggest(Integer type, Long orgId, String name);
}