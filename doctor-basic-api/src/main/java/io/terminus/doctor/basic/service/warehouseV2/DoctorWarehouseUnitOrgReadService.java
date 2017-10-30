package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseUnitOrg;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-30 16:08:20
 * Created by [ your name ]
 */
public interface DoctorWarehouseUnitOrgReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehouseUnitOrg
     */
    Response<DoctorWarehouseUnitOrg> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseUnitOrg>
     */
    Response<Paging<DoctorWarehouseUnitOrg>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseUnitOrg>
     */
    Response<List<DoctorWarehouseUnitOrg>> list(Map<String, Object> criteria);


    Response<List<DoctorBasic>> findByOrgId(Long orgId);
}