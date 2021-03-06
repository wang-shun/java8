package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.DoctorWareHouseCriteria;
import io.terminus.doctor.basic.model.DoctorWareHouse;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
public interface NewDoctorWarehouseReaderService {


    /**
     * 分页查询
     *
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWareHouse>> paging(DoctorWareHouseCriteria criteria);

    /**
     * 根据猪厂查询所有所属的仓库
     *
     * @param farmId
     * @return
     */
    Response<List<DoctorWareHouse>> findByFarmId(Long farmId);

    Response<List<DoctorWareHouse>> findByOrgId(List<Long> farmIds, Integer type);

    /**
     * 根据编号查询仓库
     *
     * @param warehouseId
     * @return
     */
    Response<DoctorWareHouse> findById(Long warehouseId);

    Response<List<DoctorWareHouse>> list(DoctorWareHouse criteria);

    /**
     * 统计仓库余额和余量
     *
     * @param warehouseId 仓库编号
     * @return
     */
    Response<AmountAndQuantityDto> countWarehouseBalance(Long warehouseId);

    Response<List<DoctorWareHouse>> getWarehouseByType(DoctorWareHouse criteria,Integer pageCurrent);

    /**
     * 按照仓库类型进行tab分页筛选，仓库按照创建时间进行排列
     */
    Response<List<Map<String,Object>>> listTypeMap(Long farmId,Integer type);

    /**
     * 展示该仓库所有物料结存数量和结存金额明细
     */
    Paging<Map<String,Object>> listDetailTypeMap(
                                                        Integer type,
                                                         String materialName,
                                                         Long warehouseId,
                                                         Integer pageNo,
                                                         Integer pageSize,
                                                         String showZero,
                                                        Integer isSettled);


    Response<DoctorWareHouse> findWareHousesByFarmAndWareHousesName(@NotNull(message = "farmId.can.not.be.null") Long farmId,
                                                                    @NotNull(message = "wareHouse.name.not.empty") String wareHouseName);


}
