package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorMaterialInWareHouseReadService {

    /**
     * 获取仓库的物料数据信息
     * @param farmId
     * @param wareHouseId
     * @return
     */
    Response<List<DoctorMaterialInWareHouse>> queryDoctorMaterialInWareHouse(@NotNull(message = "input.farmId.empty") Long farmId,
                                                                             @NotNull(message = "input.wareHouseId.empty") Long wareHouseId);

    /**
     * 分页查询仓库原料信息
     * @param farmId
     * @param wareHouseId
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorMaterialInWareHouse>> pagingDoctorMaterialInWareHouse(@NotNull(message = "input.farmId.empty") Long farmId,
                                                                                   Long wareHouseId,
                                                                                   Long materialId, String materialName,
                                                                                   Integer pageNo, Integer pageSize);

    /**
     * 通过Id 后去Material Info 信息
     * @param farmId
     * @param materialId
     * @param wareHouseId
     * @return
     */
    Response<DoctorMaterialInWareHouse> queryByMaterialWareHouseIds(@NotNull(message = "input.farmId.empty") Long farmId,
                                                                    @NotNull(message = "input.materialId.empty") Long materialId,
                                                                    @NotNull(message = "input.wareHouseId.empty") Long wareHouseId);

    /**
     * 查询对应Material in warehouse 信息内容
     * @param id
     * @return
     */
    Response<DoctorMaterialInWareHouse> queryDoctorMaterialInWareHouse(@NotNull(message = "input.id.empty")  Long id);
}
