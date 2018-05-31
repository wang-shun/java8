package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockStatisticsDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/8/26.
 */
public interface DoctorWarehouseReportReadService {


    /**
     * 统计猪厂下所有仓库的余额和余量
     *
     * @param farmId
     * @return
     */
//    Response<AmountAndQuantityDto> countFarmBalance(Long farmId);

    Response<AmountAndQuantityDto> countFarmBalance(Long farmId, Long skuId);

    /**
     * 统计猪厂下某一类型的仓库的所有余量和余额
     *
     * @param farmId
     * @param type
     * @return
     */
//    Response<AmountAndQuantityDto> countWarehouseTypeBalance(Long farmId, Integer type);

    /**
     * 统计猪厂下，各个类型的余量和余额
     *
     * @param farmId
     * @return
     */
    Response<Map<Integer, AmountAndQuantityDto>> countBalanceEachWarehouseType(Long farmId);

    /**
     * 统计仓库余额和余量
     *
     * @param warehouseId 仓库编号
     * @return
     */
    Response<AmountAndQuantityDto> countWarehouseBalance(Long warehouseId);

    /**
     * 统计猪厂下各个仓库的余额和余量
     *
     * @param farmId
     * @return
     */
    Response<Map<Long, AmountAndQuantityDto>> countEachWarehouseBalance(Long farmId, Integer warehouseType);

    Response<Map<Long/*materialID*/, AmountAndQuantityDto>> countEachMaterialBalance(Long farmId, Long warehouId);

    /**
     * 统计仓库下某一物料的余额和余量
     *
     * @param warehouseId
     * @param materialId
     * @return
     */
    Response<AmountAndQuantityDto> countMaterialBalance(Long warehouseId, Long materialId);

    /**
     * 统计仓库下某一个供应商的物料的余额和余量
     *
//     * @param warehouseId
//     * @param materialId
//     * @param vendorName
//     * @return
     */
//    Response<AmountAndQuantityDto> countMaterialBalance(Long warehouseId, Long materialId, String vendorName);


//    Response<AmountAndQuantityDto> countBalance(List<DoctorWarehousePurchase> purchases);

    Response<AmountAndQuantityDto> countBalance(Long farmId, Integer warehouseType, Long warehouseId, Long materialId, String vendorName);

    /**
     * 统计猪厂某一月份的物料处理总量和总金额
     *
     * @param farmId
     * @param handleDate
     * @return
     */
    Response<WarehouseStockStatisticsDto> countMaterialHandle(Long farmId, Calendar handleDate);

    /**
     * 统计猪厂某一月份下，不同仓库类型的物料处理总量和总金额
     *
     * @param farmId
     * @param handleDate
     * @return
     */
    Response<Map<Integer, WarehouseStockStatisticsDto>> countMaterialHandleGroupByWarehouseType(Long farmId, Calendar handleDate);

    /**
     * 统计猪厂下某一个月份下，每个仓库的物料处理总量和总金额
     *
     * @return
     */
    Response<Map<Long, WarehouseStockStatisticsDto>> countMaterialHandleEveryWarehouse(Long farmId, Calendar handleDate);

    /**
     * 统计猪厂某一月份的指定物料操作类型的总数量和总金额
     *
     * @param farmId
     * @param handleDate
     * @param types
     * @return
     */
    Response<WarehouseStockStatisticsDto> countMaterialHandle(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types);

    Response<Map<Integer, WarehouseStockStatisticsDto>> countMaterialHandleByFarmAndWarehouseType(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types);

    Response<Map<Long, WarehouseStockStatisticsDto>> countMaterialHandleByFarm(Long farmId, Integer warehouseType, Calendar handleDate, WarehouseMaterialHandleType... types);

    Response<Map<Long, WarehouseStockStatisticsDto>> countMaterialHandleByWarehouse(Long farmId, Long warehouseId, Calendar handleDate, WarehouseMaterialHandleType... types);


    /**
     * 统计仓库某一个月份，指定物料操作类型的总数量和总金额
     *
     * @param warehouseId
     * @param handleDate
     * @param types
     * @return
     */
    Response<WarehouseStockStatisticsDto> countMaterialHandleByWarehouse(Long warehouseId, Calendar handleDate, WarehouseMaterialHandleType... types);


    /**
     * 统计仓库下某一物料，在某一月份的，某些操作类型的总数量和总金额
     *
     * @param warehouseId
     * @param materialId
     * @param handleDate
     * @return
     */
    Response<WarehouseStockStatisticsDto> countMaterialHandleByMaterial(Long warehouseId, Long materialId, Calendar handleDate, WarehouseMaterialHandleType... types);

    Response<WarehouseStockStatisticsDto> countMaterialHandleByFarmAndMaterial(Long farmId, Long materialId, Calendar handleDate, WarehouseMaterialHandleType... types);

    Response<WarehouseStockStatisticsDto> countMaterialHandleByMaterialVendor(Long warehouseId, Long materialId, String vendorName, Calendar handleDate, WarehouseMaterialHandleType... types);

    Response<WarehouseStockStatisticsDto> countMaterialHandleStatistics(
            Long farmId,
            Integer warehouseType,
            Long warehouseId,
            Long materialId,
            String vendorName,
            Calendar date,
            WarehouseMaterialHandleType... types
    );

    /**
     * 获取会计年月里面所有物料数据
     * @param farmId
     * @param settlementDate
     * @param pigBarnType
     * @param pigBarnId
     * @param pigGroupId
     * @param handlerType
     * @param type
     * @param warehouseId
     * @param materialName
     * @return
     */
    List<Map<String,Object>> getMeterails(
            Long farmId,
            String settlementDate,
            Integer pigBarnType,
            Long pigBarnId,
            Long pigGroupId,
            Integer handlerType,
            Integer type,
            Long warehouseId,
            String materialName);

    /**
     * 按照仓库类型进行tab分页筛选，仓库按照创建时间进行排列
     */
    Map<String,Object> lastWlbdReport(
            Long farmId,
            String settlementDate,
            Integer pigBarnType,
            Long pigBarnId,
            Long pigGroupId,
            Integer handlerType,
            Integer type,
            Long warehouseId,
            String materialName);


    /**
     * 按照仓库类型进行tab分页筛选，仓库按照创建时间进行排列
     */
    List<Map<String,Object>> wlbdReport(
            Long farmId,
            String settlementDate,
            Integer pigBarnType,
            Long pigBarnId,
            Long pigGroupId,
            Integer handlerType,
            Integer type,
            Long warehouseId,
            String materialName);

}
