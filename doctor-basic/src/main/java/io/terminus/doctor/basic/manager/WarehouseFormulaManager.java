package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseFormulaDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 配方生产
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Slf4j
@Component
public class WarehouseFormulaManager extends AbstractStockManager<WarehouseFormulaDto.WarehouseFormulaDetail, WarehouseFormulaDto> {


    @Override
    public DoctorWarehouseMaterialHandle create(WarehouseFormulaDto.WarehouseFormulaDetail detail,
                                                WarehouseFormulaDto stockDto,
                                                DoctorWarehouseStockHandle stockHandle,
                                                DoctorWareHouse wareHouse) {

        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);
        materialHandle.setType(stockHandle.getHandleSubType());

        //新增历史配方生产出库明细
        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {

            materialHandle.setHandleDate(this.buildNewHandleDate(stockDto.getHandleDate()).getTime());

            //获取该笔明细之前的库存量，包括该事件日期
            BigDecimal historyQuantity = getHistoryQuantityInclude(materialHandle.getHandleDate(), wareHouse.getId(), detail.getMaterialId());

            materialHandle.setBeforeStockQuantity(historyQuantity);

            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.FORMULA_OUT.getValue())) {
//                historyQuantity = historyQuantity.subtract(detail.getQuantity());

                if (historyQuantity.subtract(detail.getQuantity()).compareTo(new BigDecimal(0)) < 0) {
//                    throw new ServiceException("warehouse.stock.not.enough");
                    throw new InvalidException("stock.not.enough.no.unit", wareHouse.getWareHouseName(), materialHandle.getMaterialName(), historyQuantity);
                }
                historyQuantity = historyQuantity.subtract(detail.getQuantity());
            } else {
                historyQuantity = historyQuantity.add(detail.getQuantity());
            }

            //该笔单据明细之后单据明细需要重算
            recalculate(materialHandle.getHandleDate(), false, wareHouse.getId(), detail.getMaterialId(), historyQuantity);
        } else {
            log.info("get current stock quantity for warehouse:{},sku:{}", detail.getMaterialId(), wareHouse.getId());
            BigDecimal currentQuantity = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(detail.getMaterialId(), wareHouse.getId())
                    .orElse(DoctorWarehouseStock.builder().quantity(new BigDecimal(0)).build())
                    .getQuantity();


            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.FORMULA_OUT.getValue()) &&
                    currentQuantity.compareTo(materialHandle.getQuantity()) < 0)
//                throw new ServiceException("warehouse.stock.not.enough");
                throw new InvalidException("stock.not.enough.no.unit", wareHouse.getWareHouseName(), materialHandle.getMaterialName(), currentQuantity);

            materialHandle.setBeforeStockQuantity(currentQuantity);
        }
        doctorWarehouseMaterialHandleDao.create(materialHandle);
        return materialHandle;
    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);

//        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
        recalculate(materialHandle);
//        }
    }
}
