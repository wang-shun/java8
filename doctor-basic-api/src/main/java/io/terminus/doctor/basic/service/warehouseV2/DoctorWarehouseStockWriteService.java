package io.terminus.doctor.basic.service.warehouseV2;


import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;

import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-18 09:42:25
 * Created by [ your name ]
 */
public interface DoctorWarehouseStockWriteService {


    String DEFAULT_VENDOR_NAME = "默认";

    /**
     * 创建
     *
     * @param doctorWarehouseStock
     * @return Boolean
     */
    Response<Long> create(DoctorWarehouseStock doctorWarehouseStock);

    /**
     * 更新
     *
     * @param doctorWarehouseStock
     * @return Boolean
     */
    Response<Boolean> update(DoctorWarehouseStock doctorWarehouseStock);

    /**
     * 删除
     *
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);


    /**
     * 入库
     *
     * @param stockIn
     * @return
     */
    Response<Long> in(WarehouseStockInDto stockIn);

    /**
     * 盘点
     *
     * @param stockInventory
     * @return
     */
    Response<Long> inventory(WarehouseStockInventoryDto stockInventory);

    /**
     * 调拨
     *
     * @param stockTransfer
     * @return
     */
    Response<Long> transfer(WarehouseStockTransferDto stockTransfer);

    /**
     * 出库
     *
     * @param stockOut
     * @return
     */
    Response<Long> out(WarehouseStockOutDto stockOut);

    /**
     * 退料入库
     *
     * @param stockRefundDto
     * @return
     */
    Response<Long> refund(WarehouseStockRefundDto stockRefundDto);


    /**
     * 配方生产
     *
     * @param formulaDto
     * @return
     */
    Response<Boolean> formula(WarehouseFormulaDto formulaDto);

    /**
     * 创建
     *
     * @param doctorWarehouseStock
     * @param list
     * @param dblist
     * @param doctorWarehouseMaterialApplies
     * @return
     */
    Response<Long> create(DoctorWarehouseStockHandle doctorWarehouseStock, List<DoctorWarehouseMaterialHandle> list,
                          List<DoctorWarehouseMaterialHandle> dblist,
                          List<DoctorWarehouseMaterialApply> doctorWarehouseMaterialApplies);

    /**
     * 修改
     *
     * @param doctorWarehouseStock
     * @param list
     * @param dblist
     * @param doctorWarehouseMaterialApplies
     * @return
     */
    Response<Long> update(DoctorWarehouseStockHandle doctorWarehouseStock, List<DoctorWarehouseMaterialHandle> list,
                          List<DoctorWarehouseMaterialHandle> dblist,
                          List<DoctorWarehouseMaterialApply> doctorWarehouseMaterialApplies);

//    @Deprecated
//    Response<Boolean> outAndIn(List<DoctorWarehouseStockHandleDto> inHandles, List<DoctorWarehouseStockHandleDto> outHandles, DoctorWarehouseStockHandler handle);

}