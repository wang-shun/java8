package io.terminus.doctor.basic.service.warehouseV2;

import ch.qos.logback.core.net.SyslogOutputStream;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockStatisticsDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;
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


    @Override
    public Response<AmountAndQuantityDto> countFarmBalance(Long farmId) {

        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase
                .builder()
                .farmId(farmId)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build());

        return countBalance(purchases);
    }

    @Override
    public Response<AmountAndQuantityDto> countWarehouseTypeBalance(Long farmId, Integer type) {

        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .farmId(farmId)
                .warehouseType(type)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build()));
    }

    @Override
    public Response<Map<Integer, AmountAndQuantityDto>> countBalanceEachWarehouseType(Long farmId) {


        Map<Integer, List<DoctorWarehousePurchase>> warehouseTypePurchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .farmId(farmId)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build()).stream().collect(Collectors.groupingBy(DoctorWarehousePurchase::getWarehouseType));


        Map<Integer, AmountAndQuantityDto> eachWarehouseTypeBalance = new HashMap<>();
        for (Integer warehouseType : warehouseTypePurchases.keySet()) {
            eachWarehouseTypeBalance.put(warehouseType, countBalance(warehouseTypePurchases.get(warehouseType)).getResult());
        }
        return Response.ok(eachWarehouseTypeBalance);
    }

    @Override
    public Response<AmountAndQuantityDto> countWarehouseBalance(Long warehouseId) {
        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .warehouseId(warehouseId)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build()));
    }

    @Override
    public Response<Map<Long, AmountAndQuantityDto>> countEachWarehouseBalance(Long farmId) {

        Map<Long, List<DoctorWarehousePurchase>> warehousePurchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .farmId(farmId)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build()).stream().collect(Collectors.groupingBy(DoctorWarehousePurchase::getWarehouseId));


        Map<Long, AmountAndQuantityDto> eachWarehouseBalance = new HashMap<>();
        for (Long warehouseId : warehousePurchases.keySet()) {
            eachWarehouseBalance.put(warehouseId, countBalance(warehousePurchases.get(warehouseId)).getResult());
        }

        return Response.ok(eachWarehouseBalance);
    }

    @Override
    public Response<AmountAndQuantityDto> countMaterialBalance(Long warehouseId, Long materialId) {
        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .warehouseId(warehouseId)
                .materialId(materialId)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build()));
    }

    @Override
    public Response<AmountAndQuantityDto> countMaterialBalance(Long warehouseId, Long materialId, String vendorName) {
        return countBalance(doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .warehouseId(warehouseId)
                .materialId(materialId)
                .vendorName(vendorName)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build()));
    }

    private Response<AmountAndQuantityDto> countBalance(List<DoctorWarehousePurchase> purchases) {
        if (null == purchases || purchases.isEmpty())
            return Response.ok(new AmountAndQuantityDto(0, new BigDecimal(0)));


        long totalAmount = 0;
        BigDecimal totalQuantity = new BigDecimal(0);
        for (DoctorWarehousePurchase purchase : purchases) {
            BigDecimal leftQuantity = purchase.getQuantity().multiply(purchase.getHandleQuantity());
            totalQuantity = leftQuantity.add(totalQuantity);
            totalAmount += leftQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
        }

        return Response.ok(new AmountAndQuantityDto(totalAmount, totalQuantity));
    }

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

        return countMaterialHandleByFarm(farmId, handleDate, ALL_KIND_OF_HANDLE);
    }

    @Override
    public Response<WarehouseStockStatisticsDto> countMaterialHandle(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        return Response.ok(countMaterialHandle(findMaterialHandleByFarm(farmId, handleDate, types)));
    }

    @Override
    public Response<Map<Integer, WarehouseStockStatisticsDto>> countMaterialHandleByFarmAndWarehouseType(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        Map<Integer/*warehouse type*/, WarehouseStockStatisticsDto> statistics = new HashMap<>(5);
        Map<Integer, List<DoctorWarehouseMaterialHandle>> handleGroupByWarehouseType = findMaterialHandleByFarm(farmId, handleDate, types).stream().
                collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getType));
        for (Integer type : handleGroupByWarehouseType.keySet()) {
            statistics.put(type, countMaterialHandle(handleGroupByWarehouseType.get(type)));
        }

        return Response.ok(statistics);
    }

    @Override
    public Response<Map<Long, WarehouseStockStatisticsDto>> countMaterialHandleByFarm(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        Map<Long, WarehouseStockStatisticsDto> statistics = new HashMap<>();
        Map<Long, List<DoctorWarehouseMaterialHandle>> handleGroupByWarehouse = findMaterialHandleByFarm(farmId, handleDate, types).stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getWarehouseId));
        for (Long warehouseId : handleGroupByWarehouse.keySet()) {
            statistics.put(warehouseId, countMaterialHandle(handleGroupByWarehouse.get(warehouseId)));
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
    public Response<WarehouseStockStatisticsDto> countMaterialHandleByMaterialVendor(Long warehouseId, Long materialId, String vendorName, Calendar handleDate, WarehouseMaterialHandleType... types) {
        return Response.ok(countMaterialHandle(findMaterialHandleByMaterialVendor(warehouseId, materialId, vendorName, handleDate, types)));
    }

    private WarehouseStockStatisticsDto countMaterialHandle(List<DoctorWarehouseMaterialHandle> handles) {

        if (null == handles || handles.isEmpty())
            return WarehouseStockStatisticsDto.builder()
                    .in(new AmountAndQuantityDto(0, new BigDecimal(0)))
                    .out(new AmountAndQuantityDto(0, new BigDecimal(0)))
                    .inventoryDeficit(new AmountAndQuantityDto(0, new BigDecimal(0)))
                    .inventoryProfit(new AmountAndQuantityDto(0, new BigDecimal(0)))
                    .transferIn(new AmountAndQuantityDto(0, new BigDecimal(0)))
                    .transferOut(new AmountAndQuantityDto(0, new BigDecimal(0)))
                    .build();


        long totalInAmount = 0;
        long totalOutAmount = 0;
        long totalTransferInAmount = 0;
        long totalTransferOutAmount = 0;
        long totalInventoryDeficitAmount = 0;
        long totalInventoryProfitAmount = 0;
        BigDecimal totalInQuantity = new BigDecimal(0);
        BigDecimal totalOutQuantity = new BigDecimal(0);
        BigDecimal totalInventoryDeficitQuantity = new BigDecimal(0);
        BigDecimal totalInventoryProfitQuantity = new BigDecimal(0);
        BigDecimal totalTransferInQuantity = new BigDecimal(0);
        BigDecimal totalTransferOutQuantity = new BigDecimal(0);

        for (DoctorWarehouseMaterialHandle handle : handles) {

            long amount = handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue();
            BigDecimal quantity = handle.getQuantity();

            if (WarehouseMaterialHandleType.IN.getValue() == handle.getType()) {
                totalInAmount += amount;
                totalInQuantity = totalInQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.OUT.getValue() == handle.getType()) {
                totalOutAmount += amount;
                totalOutQuantity = totalOutQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue() == handle.getType()) {
                totalInventoryDeficitAmount += amount;
                totalInventoryDeficitQuantity = totalInventoryDeficitQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == handle.getType()) {
                totalInventoryProfitAmount += amount;
                totalInventoryProfitQuantity = totalInventoryProfitQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()) {
                totalTransferInAmount += amount;
                totalTransferInQuantity = totalTransferInQuantity.add(quantity);
            } else if (WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()) {
                totalTransferOutAmount += amount;
                totalTransferOutQuantity = totalTransferOutQuantity.add(quantity);
            }
        }

        return WarehouseStockStatisticsDto.builder()
                .in(new AmountAndQuantityDto(totalInAmount, totalInQuantity))
                .out(new AmountAndQuantityDto(totalOutAmount, totalOutQuantity))
                .inventoryProfit(new AmountAndQuantityDto(totalInventoryProfitAmount, totalInventoryProfitQuantity))
                .inventoryDeficit(new AmountAndQuantityDto(totalInventoryDeficitAmount, totalInventoryDeficitQuantity))
                .transferOut(new AmountAndQuantityDto(totalTransferOutAmount, totalTransferOutQuantity))
                .transferIn(new AmountAndQuantityDto(totalTransferInAmount, totalTransferInQuantity))
                .build();
    }

    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByFarm(Long farmId, Calendar handleDate, WarehouseMaterialHandleType... types) {

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("farmId", farmId);
        criteria.put("handleYear", handleDate.get(Calendar.YEAR));
        criteria.put("handleMonth", handleDate.get(Calendar.MONTH) + 1);
        if (null != types)
            criteria.put("bigType", Stream.of(types).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));

        return doctorWarehouseMaterialHandleDao.advList(criteria);
    }

    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByWarehouse(Long warehouseId, Calendar handleDate, WarehouseMaterialHandleType... types) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", warehouseId);
        criteria.put("handleYear", handleDate.get(Calendar.YEAR));
        criteria.put("handleMonth", handleDate.get(Calendar.MONTH) + 1);
        if (null != types)
            criteria.put("bigType", Stream.of(types).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));

        return doctorWarehouseMaterialHandleDao.advList(criteria);
    }


    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByMaterial(Long warehouseId, Long materialId, Calendar handleDate, WarehouseMaterialHandleType... types) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("handleYear", handleDate.get(Calendar.YEAR));
        criteria.put("handleMonth", handleDate.get(Calendar.MONTH) + 1);
        if (null != types) {
            criteria.put("bigType", Stream.of(types).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));
        }

        return doctorWarehouseMaterialHandleDao.advList(criteria);
    }

    private List<DoctorWarehouseMaterialHandle> findMaterialHandleByMaterialVendor(Long warehouseId, Long materialId, String vendorName, Calendar handleDate, WarehouseMaterialHandleType... types) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("handleYear", handleDate.get(Calendar.YEAR));
        criteria.put("handleMonth", handleDate.get(Calendar.MONTH) + 1);
        criteria.put("vendorName", vendorName);
        if (null != types)
            criteria.put("bigType", Stream.of(types).map(WarehouseMaterialHandleType::getValue).collect(Collectors.toList()));

        return doctorWarehouseMaterialHandleDao.advList(criteria);
    }

}
