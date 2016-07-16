package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;

/**
 * Desc: 基础物料表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */

public interface DoctorBasicMaterialWriteService {

    /**
     * 创建DoctorBasicMaterial
     * @param basicMaterial 基础物料表实例
     * @return 主键id
     */
    Response<Long> createBasicMaterial(DoctorBasicMaterial basicMaterial);

    /**
     * 更新DoctorBasicMaterial
     * @param basicMaterial 基础物料表实例
     * @return 是否成功
     */
    Response<Boolean> updateBasicMaterial(DoctorBasicMaterial basicMaterial);

    /**
     * 根据主键id删除DoctorBasicMaterial
     * @param basicMaterialId 基础物料表实例主键id
     * @return 是否成功
     */
    Response<Boolean> deleteBasicMaterialById(Long basicMaterialId);
}