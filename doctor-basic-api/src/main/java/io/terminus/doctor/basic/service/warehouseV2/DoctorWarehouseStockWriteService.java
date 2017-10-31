package io.terminus.doctor.basic.service.warehouseV2;


import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.*;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandler;

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
    Response<Boolean> in(WarehouseStockInDto stockIn);

    /**
     * 盘点
     *
     * @param stockInventory
     * @return
     */
    Response<Boolean> inventory(WarehouseStockInventoryDto stockInventory);

    /**
     * 调拨
     *
     * @param stockTransfer
     * @return
     */
    Response<Boolean> transfer(WarehouseStockTransferDto stockTransfer);

    /**
     * 出库
     *
     * @param stockOut
     * @return
     */
    Response<Boolean> out(WarehouseStockOutDto stockOut);


    /**
     * 配方生产
     * @param formulaDto
     * @return
     */
    Response<Boolean> formula(WarehouseFormulaDto formulaDto);

    @Deprecated
    Response<Boolean> outAndIn(List<DoctorWarehouseStockHandleDto> inHandles, List<DoctorWarehouseStockHandleDto> outHandles, DoctorWarehouseStockHandler handle);

}