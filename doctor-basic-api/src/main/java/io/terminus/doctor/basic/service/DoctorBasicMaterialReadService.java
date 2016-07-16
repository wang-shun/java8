package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorBasicMaterialSearchDto;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;

import javax.validation.constraints.NotNull;

/**
 * Desc: 基础物料表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */

public interface DoctorBasicMaterialReadService {

    /**
     * 根据id查询基础物料表
     * @param basicMaterialId 主键id
     * @return 基础物料表
     */
    Response<DoctorBasicMaterial> findBasicMaterialById(@NotNull(message = "basicMaterialId.not.null") Long basicMaterialId);

    /**
     * 分页查询基础物料
     * @param basicMaterial 基础物料
     * @return 基础物料list
     */
    Response<Paging<DoctorBasicMaterial>> pagingBasicMaterialByTypeFilterBySrm(DoctorBasicMaterialSearchDto basicMaterial);

}
