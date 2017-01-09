package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorBasicMaterialSearchDto;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;

import javax.validation.constraints.NotNull;
import java.util.List;

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


    /**
     * 查询全部基础物料(可以根据输入码过滤)
     * @param type 基础物料类型
     * @see io.terminus.doctor.common.enums.WareHouseType
     * @param srm 输入码
     * @param exIds 排除掉的ids
     * @return 基础物料list
     */
    Response<List<DoctorBasicMaterial>> findBasicMaterialByTypeFilterBySrm(Integer type, String srm, String exIds);

    /**
     * 查询所有的物料数据
     * @return
     */
    Response<List<DoctorBasicMaterial>> findAllBasicMaterials();

    /**
     * 查询猪场有权限的物料数据
     * @param farmId
     * @param type
     * @return
     */
    Response<List<DoctorBasicMaterial>> findBasicMaterialsOwned(Long farmId, Long type, String srm);
}
