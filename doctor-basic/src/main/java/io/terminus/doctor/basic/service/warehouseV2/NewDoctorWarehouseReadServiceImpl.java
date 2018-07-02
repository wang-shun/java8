package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorFarmBasicDao;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.DoctorWareHouseCriteria;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.service.warehouseV2.NewDoctorWarehouseReaderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
@Slf4j
@Service
@RpcProvider
public class NewDoctorWarehouseReadServiceImpl implements NewDoctorWarehouseReaderService {

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Autowired
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;


    @Override
    public Response<Paging<DoctorWareHouse>> paging(DoctorWareHouseCriteria criteria) {
        PageInfo pageInfo = new PageInfo(criteria.getPageNo(), criteria.getPageSize());


        Map<String, Object> param = new HashedMap();
        param.put("farmId", criteria.getFarmId());
        param.put("type", criteria.getType());
        Paging<DoctorWareHouse> wareHousePaging = doctorWareHouseDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), param);

        return Response.ok(wareHousePaging);
    }


    @Override
    public Response<List<DoctorWareHouse>> list(DoctorWareHouse criteria) {
        try {
            return Response.ok(doctorWareHouseDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouseV2, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWareHouse>> findByFarmId(Long farmId) {
        try {
            return Response.ok(doctorWareHouseDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.find.fail");
        }
    }

    @Override
    public Response<List<DoctorWareHouse>> findByOrgId(List<Long> farmIds, Integer type) {
        try {

            List<DoctorWareHouse> wareHouses = new ArrayList<>();
            for (Long farmId : farmIds) {
                List<DoctorWareHouse> allTypeWarehouses = doctorWareHouseDao.findByFarmId(farmId);
                if (null != type) {
                    for (DoctorWareHouse wareHouse : allTypeWarehouses) {
                        if (wareHouse.getType().equals(type))
                            wareHouses.add(wareHouse);
                    }
                } else
                    wareHouses.addAll(allTypeWarehouses);
            }
            return Response.ok(wareHouses);
        } catch (Exception e) {
            log.error("failed to find doctor warehouseV2, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.find.fail");
        }
    }

    @Override
    public Response<DoctorWareHouse> findById(Long warehouseId) {
        try {
            return Response.ok(doctorWareHouseDao.findById(warehouseId));
        } catch (Exception e) {
            log.error("failed to find doctor warehouseV2, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.find.fail");
        }
    }

    @Override
    public Response<AmountAndQuantityDto> countWarehouseBalance(Long warehouseId) {
        List<DoctorWarehousePurchase> warehousePurchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .warehouseId(warehouseId)
                .build());

        if (null == warehousePurchases || warehousePurchases.isEmpty()) {
            log.debug("该仓库[{}]已出库完", warehouseId);
            return Response.ok(new AmountAndQuantityDto());
        }

        BigDecimal totalQuantity = new BigDecimal(0);
        BigDecimal totalAmount = new BigDecimal(0);
        for (DoctorWarehousePurchase purchase : warehousePurchases) {
            BigDecimal leftQuantity = purchase.getQuantity().subtract(purchase.getHandleQuantity());
            totalQuantity = totalQuantity.add(leftQuantity);
            totalAmount = totalAmount.add(leftQuantity.multiply(new BigDecimal(purchase.getUnitPrice())));
        }
        return Response.ok(new AmountAndQuantityDto(totalAmount, totalQuantity));
    }

    @Override
    public Response<List<DoctorWareHouse>> getWarehouseByType(DoctorWareHouse criteria, Integer pageCurrent) {
        try {
            int pageNum = 6;
            int pageSize = (pageCurrent - 1) * pageNum;
            return Response.ok(doctorWareHouseDao.getWarehouseByType(criteria, pageSize, pageNum));
        } catch (Exception e) {
            log.error("failed to list doctor warehouseV2, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.list.fail");
        }
    }

    /**
     * 按照仓库类型进行tab分页筛选，仓库按照创建时间进行排列
     *
     * @param farmId
     * @param type
     * @return
     */
    @Override
    public Response<List<Map<String, Object>>> listTypeMap(Long farmId, Integer type) {
        //会计年月
        Date settlementDate = doctorWarehouseSettlementService.getSettlementDate(new Date());
        return Response.ok(doctorWareHouseDao.listTypeMap(farmId, type,settlementDate));
    }

    @Override
    public Paging<Map<String, Object>> listDetailTypeMap(Integer type,
                                                                 String materialName,
                                                                 Long warehouseId,
                                                                 Integer pageNo,
                                                                 Integer pageSize,
                                                                 String showZero,
                                                         Integer isSettled) {
        PageInfo pageInfo = new PageInfo(pageNo, pageSize);
        Map<String, Object> param = new HashedMap();
        param.put("type", type);
        param.put("materialName", materialName);
        param.put("warehouseId", warehouseId);
        param.put("showZero",showZero);
        param.put("isSettled",isSettled);
        //会计年月
        Date settlementDate = doctorWarehouseSettlementService.getSettlementDate(new Date());
        param.put("settlementDate",settlementDate);
        return doctorWareHouseDao.listDetailTypeMap(
                pageInfo.getOffset(), pageInfo.getLimit(), param);
    }

    @Override
    public Response<DoctorWareHouse> findWareHousesByFarmAndWareHousesName(@NotNull(message = "farmId.can.not.be.null")Long farmId, @NotNull(message = "wareHouse.name.not.empty") String wareHouseName) {
        try {
            return Response.ok(doctorWareHouseDao.findWareHousesByFarmAndWareHousesName(ImmutableMap.of("farmId", farmId, "name", wareHouseName)));
        } catch (Exception e) {
            log.error("find WareHouse by farm and wareHouse name failed, farmId:{}, wareHouseName:{}, cause:{}", farmId, wareHouseName, Throwables.getStackTraceAsString(e));
            return Response.fail("find.WareHouse.by.farm.and.wareHouse.name.failed");
        }
    }
}
