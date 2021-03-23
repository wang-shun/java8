package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockMonthlyDao;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockStatisticsDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2017/8/27.
 */
@Service
@RpcProvider
public class DoctorWarehouseReportReadServiceImpl implements DoctorWarehouseReportReadService {

    private static final WarehouseMaterialHandleType[] ALL_KIND_OF_HANDLE = new WarehouseMaterialHandleType[]{
            WarehouseMaterialHandleType.IN,
            WarehouseMaterialHandleType.OUT,
            WarehouseMaterialHandleType.INVENTORY_DEFICIT,
            WarehouseMaterialHandleType.INVENTORY_PROFIT,
            WarehouseMaterialHandleType.TRANSFER_IN,
            WarehouseMaterialHandleType.TRANSFER_OUT
    };

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;

//    @Override
//    public Response<AmountAndQuantityDto> countFarmBalance(Long farmId) {

//        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase
//                .builder()
//                .farmId(farmId)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build());
//
//        return countBalance(purchases);

//        doctorWarehouseStockMonthlyDao.list(DoctorWarehouseStockMonthly.builder()
//
//                .build());
//    }

    @Override
    public Response<AmountAndQuantityDto> countFarmBalance(Long farmId, Long skuId) {

        Map<String, Object> params = new HashMap<>();
        params.put("warehouseIds", doctorWareHouseDao.findByFarmId(farmId).stream().map(DoctorWareHouse::getId).collect(Collectors.toList()));
        params.put("skuId", skuId);

        return Response.ok(doctorWarehouseStockMonthlyDao.statistics(params));

//        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase
//                .builder()
//                .farmId(farmId)
//                .materialId(skuId)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build());
//
//        return countBalance(purchases);
    }

//    @Override
//    public Response<AmountAndQuantityDto> countWarehouseTypeBalance(Long farmId, Integer type) {
//
//        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .farmId(farmId)
//                .warehouseType(type)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()));
//    }

    @Override
    public Response<Map<Integer, AmountAndQuantityDto>> countBalanceEachWarehouseType(Long farmId) {


        Map<Integer, AmountAndQuantityDto> eachWarehouseTypeBalance = new HashMap<>();

        doctorWareHouseDao.findByFarmId(farmId).stream().collect(Collectors.groupingBy(DoctorWareHouse::getType)).forEach((k, v) -> {
            Map<String, Object> params = new HashMap<>();
            BigDecimal amount = new BigDecimal(0);
            BigDecimal quantity = new BigDecimal(0);
            for (DoctorWareHouse w : v) {
                params.put("warehouseId", w.getId());
                AmountAndQuantityDto amountAndQuantityDto = doctorWarehouseStockMonthlyDao.statistics(params);
                amount = amount.add(amountAndQuantityDto.getAmount());
                quantity = quantity.add(amountAndQuantityDto.getQuantity());
            }
            eachWarehouseTypeBalance.put(k, new AmountAndQuantityDto(amount, quantity));
        });

        return Response.ok(eachWarehouseTypeBalance);

//        Map<Integer, List<DoctorWarehousePurchase>> warehouseTypePurchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .farmId(farmId)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()).stream().collect(Collectors.groupingBy(DoctorWarehousePurchase::getWarehouseType));
//
//
//        Map<Integer, AmountAndQuantityDto> eachWarehouseTypeBalance = new HashMap<>();
//        for (Integer warehouseType : warehouseTypePurchases.keySet()) {
//            eachWarehouseTypeBalance.put(warehouseType, countBalance(warehouseTypePurchases.get(warehouseType)).getResult());
//        }
//        return Response.ok(eachWarehouseTypeBalance);
    }

    @Override
    public Response<AmountAndQuantityDto> countWarehouseBalance(Long warehouseId) {

        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        return Response.ok(doctorWarehouseStockMonthlyDao.statistics(params));
//        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .warehouseId(warehouseId)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()));
    }

    @Override
    public Response<Map<Long, AmountAndQuantityDto>> countEachWarehouseBalance(Long farmId, Integer warehouseType) {

        Map<Long, AmountAndQuantityDto> eachWarehouseBalance = new HashMap<>();
        doctorWareHouseDao.list(DoctorWareHouse.builder().farmId(farmId).type(warehouseType).build()).stream().forEach(w -> {
            Map<String, Object> params = new HashMap<>();
                params.put("warehouseId", w.getId());
            eachWarehouseBalance.put(w.getId(), doctorWarehouseStockMonthlyDao.statistics(params));
        });
        return Response.ok(eachWarehouseBalance);
//        Map<Long, List<DoctorWarehousePurchase>> warehousePurchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .farmId(farmId)
//                .warehouseType(warehouseType)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()).stream().collect(Collectors.groupingBy(DoctorWarehousePurchase::getWarehouseId));
//
//
//        如果仓库没有余额和余量，则为null
//        Map<Long, AmountAndQuantityDto> eachWarehouseBalance = new HashMap<>();
//        for (Long warehouseId : warehousePurchases.keySet()) {
//            eachWarehouseBalance.put(warehouseId, countBalance(warehousePurchases.get(warehouseId)).getResult());
//        }
//
//        return Response.ok(eachWarehouseBalance);
    }

    @Override
    public Response<Map<Long, AmountAndQuantityDto>> countEachMaterialBalance(Long farmId, Long warehouseId) {


        return Response.ok(doctorWarehouseStockMonthlyDao.statisticsGroupBySku(warehouseId, null));
//        Map<Long, List<DoctorWarehousePurchase>> materialPurchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .farmId(farmId)
//                .warehouseId(warehouId)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()).stream().collect(Collectors.groupingBy(DoctorWarehousePurchase::getMaterialId));
//        Map<Long, AmountAndQuantityDto> eachMaterialBalance = new HashMap<>();
//        for (Long materialId : materialPurchases.keySet()) {
//            eachMaterialBalance.put(materialId, countBalance(materialPurchases.get(materialId)).getResult());
//        }
//
//        return Response.ok(eachMaterialBalance);
    }

    @Override
    public Response<AmountAndQuantityDto> countMaterialBalance(Long warehouseId, Long materialId) {

        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("skuId", materialId);
        return Response.ok(doctorWarehouseStockMonthlyDao.statistics(params));
//        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .warehouseId(warehouseId)
//                .materialId(materialId)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()));
    }

//    @Override
//    public Response<AmountAndQuantityDto> countMaterialBalance(Long warehouseId, Long materialId, String vendorName) {
//        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .warehouseId(warehouseId)
//                .materialId(materialId)
//                .vendorName(vendorName)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()));
//    }

    @Override
    public Response<AmountAndQuantityDto> countBalance(Long farmId, Integer warehouseType, Long warehouseId, Long materialId, String vendorName) {


        BigDecimal amount = new BigDecimal(0);
        BigDecimal quantity = new BigDecimal(0);
        List<DoctorWareHouse> wareHouses;
        if (null != warehouseId)
            wareHouses = Collections.singletonList(doctorWareHouseDao.findById(warehouseId));
        else
            wareHouses = doctorWareHouseDao.list(DoctorWareHouse.builder().id(warehouseId).farmId(farmId).type(warehouseType).build());
        for (DoctorWareHouse wareHouse : wareHouses) {
            Map<String, Object> params = new HashMap<>();
            params.put("warehouseId", wareHouse.getId());
            params.put("skuId", materialId);
            AmountAndQuantityDto a = doctorWarehouseStockMonthlyDao.statistics(params);
            amount = amount.add(a.getAmount());
            quantity = quantity.add(a.getQuantity());

        }
        return Response.ok(new AmountAndQuantityDto(amount, quantity));
//        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
//                .farmId(farmId)
//                .warehouseType(warehouseType)
//                .warehouseId(warehouseId)
//                .materialId(materialId)
//                .vendorName(vendorName)
//                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                .build()));
    }

//    @Override
//    public Response<AmountAndQuantityDto> countBalance(List<DoctorWarehousePurchase> purchases) {
//        if (null == purchases || purchases.isEmpty())
//            return Response.ok(new AmountAndQuantityDto(0, new BigDecimal(0)));
//
//
//        long totalAmount = 0;
//        BigDecimal totalQuantity = new BigDecimal(0);
//        for (DoctorWarehousePurchase purchase : purchases) {
//            BigDecimal leftQuantity = purchase.getQuantity().subtract(purchase.getHandleQuantity());
//            totalQuantity = leftQuantity.add(totalQuantity);
//            totalAmount += leftQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
//        }
//
//        return Response.ok(new AmountAndQuantityDto(totalAmount, totalQuantity));
//    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandle(Long farmId, Calendar handleDate) {

        //数据量一大就不行。
        //最理想，通过消息队列等方式将数据同步给独立的报表系统。报表中通过汇总表方式可以快速查询
        return countMaterialHandle(farmId, handleDate, ALL_KIND_OF_HANDLE);
    }

    @Override
    public Response<Map<Integer, WarehouseStockStatisticsDto>> countMaterialHandleGroupByWarehouseType(Long farmId, Calendar handleDate) {

        return countMaterialHandleByFarmAndWarehouseType(farmId, handleDate, ALL_KIND_OF_HANDLE);
    }

    @Override
    public Response<Map<Long, WarehouseStockStatisticsDto>> countMaterialHandleEveryWarehouse(Long farmId, Calendar handleDate) {

        return countMaterialHandleByFarm(farmId, null, handleDate, ALL_KIND_OF_HANDLE);
    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandle(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        return Response.ok(countMaterialHandle(findMaterialHandleByFarm(farmId, null, handleDate, types)));
    }

    @Override
    public Response<Map<Integer, WarehouseStockStatisticsDto>> countMaterialHandleByFarmAndWarehouseType(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        Map<Integer/*warehouse type*/, WarehouseStockStatisticsDto> statistics = new HashMap<>(5);
        Map<Integer, List<DoctorWarehouseMaterialHandle>> handleGroupByWarehouseType = findMaterialHandleByFarm(farmId, null, handleDate, types).stream().
                collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getType));
        for (Integer type : handleGroupByWarehouseType.keySet()) {
            statistics.put(type, countMaterialHandle(handleGroupByWarehouseType.get(type)));
        }

        return Response.ok(statistics);
    }

    @Override
    public Response<Map<Long, WarehouseStockStatisticsDto>> countMaterialHandleByFarm(Long farmId, Integer warehouseType, Calendar handleDate, WarehouseMaterialHandleType... types) {

        Map<Long, WarehouseStockStatisticsDto> statistics = new HashMap<>();
        Map<Long, List<DoctorWarehouseMaterialHandle>> handleGroupByWarehouse = findMaterialHandleByFarm(farmId, warehouseType, handleDate, types).stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getWarehouseId));
        for (Long warehouseId : handleGroupByWarehouse.keySet()) {
            statistics.put(warehouseId, countMaterialHandle(handleGroupByWarehouse.get(warehouseId)));
        }

        return Response.ok(statistics);
    }

    @Override
    public Response<Map<Long, WarehouseStockStatisticsDto>> countMaterialHandleByWarehouse(Long farmId, Long warehouseId, Calendar handleDate, WarehouseMaterialHandleType... types) {
        Map<Long, WarehouseStockStatisticsDto> statistics = new HashMap<>();
        Map<Long, List<DoctorWarehouseMaterialHandle>> handleGroupByMaterial = findMaterialHandleByWarehouse(warehouseId, handleDate, types).stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));
        for (Long materialId : handleGroupByMaterial.keySet()) {
            statistics.put(materialId, countMaterialHandle(handleGroupByMaterial.get(materialId)));
        }

        return Response.ok(statistics);
    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandleByWarehouse(Long warehouseId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        return Response.ok(countMaterialHandle(findMaterialHandleByWarehouse(warehouseId, handleDate, types)));

    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandleByMaterial(Long warehouseId, Long materialId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        return Response.ok(countMaterialHandle(findMaterialHandleByMaterial(warehouseId, materialId, handleDate, types)));
    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandleByFarmAndMaterial(Long farmId, Long materialId, Calendar handleDate, WarehouseMaterialHandleType... types) {
        return Response.ok(countMaterialHandle(findMaterialHandleByFarm(farmId, null, handleDate, types)));
    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandleByMaterialVendor(Long warehouseId, Long materialId, String vendorName, Calendar handleDate, WarehouseMaterialHandleType... types) {
        return Response.ok(countMaterialHandle(findMaterialHandleByMaterialVendor(null, null, warehouseId, materialId, vendorName, handleDate, types)));
    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandleStatistics(Long farmId, Integer warehouseType, Long warehouseId, Long materialId, String vendorName, Calendar date, WarehouseMaterialHandleType... types) {


        Map<String, Object> criteria = new HashMap<>();
        criteria.put("farmId", farmId);
        criteria.put("warehouseType", warehouseType);
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("handleYear", date.get(Calendar.YEAR));
        criteria.put("handleMonth", date.get(Calendar.MONTH) + 1);
        criteria.put("vendorName", vendorName);
        if (null == types)
            criteria.put("bigType", Stream.of(ALL_KIND_OF_HANDLE).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));
        else
            criteria.put("bigType", Stream.of(types).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));

        List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.advList(criteria);

        return Response.ok(countMaterialHandle(handles));
    }

    private WarehouseStockStatisticsDto countMaterialHandle(List<DoctorWarehouseMaterialHandle> handles) {

        if (null == handles || handles.isEmpty())
            return WarehouseStockStatisticsDto.builder()
                    .in(new AmountAndQuantityDto())
                    .out(new AmountAndQuantityDto())
                    .inventoryDeficit(new AmountAndQuantityDto())
                    .inventoryProfit(new AmountAndQuantityDto())
                    .transferIn(new AmountAndQuantityDto())
                    .transferOut(new AmountAndQuantityDto())
                    .formulaIn(new AmountAndQuantityDto())
                    .formulaOut(new AmountAndQuantityDto())
                    .build();


        BigDecimal totalInAmount = new BigDecimal(0);
        BigDecimal totalOutAmount = new BigDecimal(0);
        BigDecimal totalTransferInAmount = new BigDecimal(0);
        BigDecimal totalTransferOutAmount = new BigDecimal(0);
        BigDecimal totalInventoryDeficitAmount = new BigDecimal(0);
        BigDecimal totalInventoryProfitAmount = new BigDecimal(0);
        BigDecimal totalFormulaInAmount = new BigDecimal(0);
        BigDecimal totalFormulaOutAmount = new BigDecimal(0);
        BigDecimal totalInQuantity = new BigDecimal(0);
        BigDecimal totalOutQuantity = new BigDecimal(0);
        BigDecimal totalInventoryDeficitQuantity = new BigDecimal(0);
        BigDecimal totalInventoryProfitQuantity = new BigDecimal(0);
        BigDecimal totalTransferInQuantity = new BigDecimal(0);
        BigDecimal totalTransferOutQuantity = new BigDecimal(0);
        BigDecimal totalFormulaInQuantity = new BigDecimal(0);
        BigDecimal totalFormulaOutQuantity = new BigDecimal(0);

        for (DoctorWarehouseMaterialHandle handle : handles) {

            BigDecimal amount = handle.getQuantity().multiply(handle.getUnitPrice());
            BigDecimal quantity = handle.getQuantity();

            if (WarehouseMaterialHandleType.IN.getValue() == handle.getType()) {
                totalInAmount = totalInAmount.add(amount);
                totalInQuantity = totalInQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.OUT.getValue() == handle.getType()) {
                totalOutAmount = totalOutAmount.add(amount);
                totalOutQuantity = totalOutQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue() == handle.getType()) {
                totalInventoryDeficitAmount = totalInventoryDeficitAmount.add(amount);
                totalInventoryDeficitQuantity = totalInventoryDeficitQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == handle.getType()) {
                totalInventoryProfitAmount = totalInventoryProfitAmount.add(amount);
                totalInventoryProfitQuantity = totalInventoryProfitQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()) {
                totalTransferInAmount = totalTransferInAmount.add(amount);
                totalTransferInQuantity = totalTransferInQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()) {
                totalTransferOutAmount = totalTransferOutAmount.add(amount);
                totalTransferOutQuantity = totalTransferOutQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.FORMULA_IN.getValue() == handle.getType()) {
                totalFormulaInAmount = totalFormulaInAmount.add(amount);
                totalFormulaInQuantity = totalFormulaInQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.FORMULA_OUT.getValue() == handle.getType()) {
                totalFormulaOutAmount = totalFormulaOutAmount.add(amount);
                totalFormulaOutQuantity = totalFormulaOutQuantity.add(quantity);
            }
        }

        return WarehouseStockStatisticsDto.builder()
                .in(new AmountAndQuantityDto(totalInAmount, totalInQuantity))
                .out(new AmountAndQuantityDto(totalOutAmount, totalOutQuantity))
                .inventoryProfit(new AmountAndQuantityDto(totalInventoryProfitAmount, totalInventoryProfitQuantity))
                .inventoryDeficit(new AmountAndQuantityDto(totalInventoryDeficitAmount, totalInventoryDeficitQuantity))
                .transferOut(new AmountAndQuantityDto(totalTransferOutAmount, totalTransferOutQuantity))
                .transferIn(new AmountAndQuantityDto(totalTransferInAmount, totalTransferInQuantity))
                .formulaIn(new AmountAndQuantityDto(totalFormulaInAmount, totalFormulaInQuantity))
                .formulaOut(new AmountAndQuantityDto(totalFormulaOutAmount, totalFormulaOutQuantity))
                .build();
    }

    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByFarm(Long farmId, Integer warehouseType, Calendar handleDate, WarehouseMaterialHandleType... types) {

        return findMaterialHandleByMaterialVendor(farmId, warehouseType, null, null, null, handleDate, types);
    }

    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByWarehouse(Long warehouseId, Calendar handleDate, WarehouseMaterialHandleType... types) {
        return findMaterialHandleByMaterialVendor(null, null, warehouseId, null, null, handleDate, types);
    }


    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByMaterial(Long warehouseId, Long materialId, Calendar handleDate, WarehouseMaterialHandleType... types) {
        return findMaterialHandleByMaterialVendor(null, null, warehouseId, materialId, null, handleDate, types);
    }

    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByMaterialVendor(Long farmId, Integer warehouseType, Long warehouseId, Long materialId, String vendorName, Calendar handleDate, WarehouseMaterialHandleType... types) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("farmId", farmId);
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("warehouseType", warehouseType);
        criteria.put("handleYear", handleDate.get(Calendar.YEAR));
        criteria.put("handleMonth", handleDate.get(Calendar.MONTH) + 1);
        criteria.put("deleteFlag", WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        criteria.put("vendorName", vendorName);
        if (null == types)
            criteria.put("bigType",   Stream.of(ALL_KIND_OF_HANDLE).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));
        else
            criteria.put("bigType", Stream.of(types).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));

        return doctorWarehouseMaterialHandleDao.advList(criteria);
    }

    @Override
    public Map<String, Object> lastWlbdReport(
            Long farmId, String settlementDate, Integer type, Long warehouseId, Long materialId) {
        Map<String,Object> lists = doctorWarehouseMaterialHandleDao.lastWlbdReport(
                farmId, settlementDate, type, warehouseId, materialId);
        return lists;
    }

    @Override
    public List<Map<String, Object>> wlbdReport(
            Long farmId, String settlementDate, Integer type, Long warehouseId, Long materialId) {
        List<Map<String,Object>> lists = doctorWarehouseMaterialHandleDao.wlbdReport(
                farmId, settlementDate, type, warehouseId, materialId);
        return lists;
    }

    /**
     * 获取会计年月里面所有物料数据
     * @param farmId
     * @param settlementDate
     * @param type
     * @param warehouseId
     * @param materialName
     * @return
     */
    @Override
    public List<Map<String,Object>> getMeterails(
            Long orgId,
            Long farmId,
            String settlementDate,
            Integer type,
            Long warehouseId,
            String materialName){
        List<Map<String,Object>> lists = doctorWarehouseMaterialHandleDao.getMeterails(
                orgId,
                farmId, settlementDate, type, warehouseId, materialName);
        return lists;
    }

}
