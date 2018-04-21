package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购入库
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseInManager extends AbstractStockManager<WarehouseStockInDto.WarehouseStockInDetailDto, WarehouseStockInDto> {


    @Override
    public void create(WarehouseStockInDto.WarehouseStockInDetailDto detail,
                       WarehouseStockInDto stockDto,
                       DoctorWarehouseStockHandle stockHandle,
                       DoctorWareHouse wareHouse) {


        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);
        materialHandle.setType(WarehouseMaterialHandleType.IN.getValue());
        materialHandle.setUnitPrice(detail.getUnitPrice());
        materialHandle.setAmount(detail.getAmount());

        //入库类型，当天第一笔
        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {

            materialHandle.setHandleDate(new DateTime(materialHandle.getHandleDate()).withTime(0, 0, 0, 0).toDate());

            //获取该笔明细之前的库存量
            BigDecimal historyQuantity = getHistoryQuantityInclude(stockDto.getHandleDate().getTime(), wareHouse.getId(), detail.getMaterialId());

            materialHandle.setBeforeStockQuantity(historyQuantity);
            historyQuantity = historyQuantity.add(detail.getQuantity());

            //需要重算每个明细的beforeStockQuantity
            recalculate(stockDto.getHandleDate().getTime(), false, wareHouse.getId(), detail.getMaterialId(), historyQuantity);
        } else {
            BigDecimal currentQuantity = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(detail.getMaterialId(), wareHouse.getId())
                    .orElse(DoctorWarehouseStock.builder().quantity(new BigDecimal(0)).build())
                    .getQuantity();
            materialHandle.setBeforeStockQuantity(currentQuantity);
        }

        doctorWarehouseMaterialHandleDao.create(materialHandle);
    }


    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);

        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
            //删除历史单据明细
            recalculate(materialHandle);
        }
    }

    public void updateQuantity(DoctorWarehouseMaterialHandle materialHandle, BigDecimal newQuantity) {

        materialHandle.setQuantity(newQuantity);
        materialHandle.setUnitPrice(materialHandle.getAmount().divide(materialHandle.getQuantity(), 4, BigDecimal.ROUND_HALF_UP));
    }
}
