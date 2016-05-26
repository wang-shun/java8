package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorMaterialInfoReadService {

    /**
     * 获取对应猪场的原料信息
     * @param farmId
     * @return
     */
    Response<List<DoctorMaterialInfo>> queryMaterialInfos(@NotNull(message = "input.farmId.empty") Long farmId);

    /**
     * 分页查询公司的原料信息
     * @param farmId
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorMaterialInfo>> pagingMaterialInfos(@NotNull(message = "input.farmId.empty") Long farmId,
                                                             Integer type, Integer pageNo, Integer pageSize);

    /**
     * id 查询对应的物料信息
     * @param id
     * @return
     */
    Response<DoctorMaterialInfo> queryById(@NotNull(message = "input.id.empty") Long id);
}

