package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialApplyType;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * 调拨
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseTransferManager extends AbstractStockManager {


    @Override
    public void create(AbstractWarehouseStockDetail detail, AbstractWarehouseStockDto stockDto, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {

        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);
        materialHandle.setType(stockHandle.getHandleSubType());

        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {

            materialHandle.setHandleDate(this.buildNewHandleDate(WarehouseMaterialHandleType.fromValue(stockHandle.getHandleSubType()), stockDto.getHandleDate()));

            //获取该笔明细之前的库存量，包括该事件日期
            BigDecimal historyQuantity = getHistoryQuantityInclude(stockDto.getHandleDate().getTime(), wareHouse.getId(), detail.getMaterialId());

            materialHandle.setBeforeStockQuantity(historyQuantity);

            historyQuantity = historyQuantity.subtract(detail.getQuantity());

            if (historyQuantity.compareTo(new BigDecimal(0)) < 0) {
                throw new ServiceException("warehouse.stock.not.enough");
            }

            //该笔单据明细之后单据明细需要重算
            recalculate(stockDto.getHandleDate().getTime(), false, wareHouse.getId(), detail.getMaterialId(), historyQuantity);
        } else {
            BigDecimal currentQuantity = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(detail.getMaterialId(), wareHouse.getId())
                    .orElse(DoctorWarehouseStock.builder().quantity(new BigDecimal(0)).build())
                    .getQuantity();

            if (currentQuantity.compareTo(materialHandle.getQuantity()) < 0)
                throw new ServiceException("warehouse.stock.not.enough");

            materialHandle.setBeforeStockQuantity(currentQuantity);
        }
        doctorWarehouseMaterialHandleDao.create(materialHandle);
    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);

        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
            recalculate(materialHandle);
        }
    }
}
