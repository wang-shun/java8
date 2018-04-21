package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockOutDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialApplyType;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * 领料出库
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseOutManager extends AbstractStockManager<WarehouseStockOutDto.WarehouseStockOutDetail, WarehouseStockOutDto> {


    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    public void create(WarehouseStockOutDto.WarehouseStockOutDetail detail,
                       WarehouseStockOutDto stockDto,
                       DoctorWarehouseStockHandle stockHandle,
                       DoctorWareHouse wareHouse) {
        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);
        materialHandle.setType(WarehouseMaterialHandleType.OUT.getValue());

        //出库类型，当天最后一笔
        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {

            materialHandle.setHandleDate(this.buildNewHandleDate(WarehouseMaterialHandleType.OUT, stockDto.getHandleDate()));

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

        DoctorWarehouseMaterialApply apply = new DoctorWarehouseMaterialApply();
        apply.setFarmId(materialHandle.getFarmId());
        apply.setWarehouseId(materialHandle.getWarehouseId());
        apply.setWarehouseName(materialHandle.getWarehouseName());
        apply.setWarehouseType(materialHandle.getWarehouseType());
        apply.setOrgId(materialHandle.getOrgId());
        apply.setSettlementDate(materialHandle.getSettlementDate());
        apply.setQuantity(materialHandle.getQuantity());
        apply.setPigBarnId(detail.getApplyPigBarnId());
        apply.setPigBarnName(detail.getApplyPigBarnName());
        apply.setPigGroupId(detail.getApplyPigGroupId());
        apply.setApplyStaffId(detail.getApplyStaffId());
        apply.setApplyStaffName(detail.getApplyStaffName());
        apply.setPigType(detail.getPigType());
        apply.setApplyDate(materialHandle.getHandleDate());
        apply.setApplyYear(stockDto.getHandleDate().get(Calendar.YEAR));
        apply.setApplyMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
        if (detail.getApplyPigGroupId() != null) {
            if (detail.getApplyPigGroupId().equals(-1L)) {
                apply.setPigGroupName("母猪");
                apply.setApplyType(WarehouseMaterialApplyType.SOW.getValue());
            } else {
                apply.setPigGroupName(detail.getApplyPigGroupName());
                apply.setApplyType(WarehouseMaterialApplyType.GROUP.getValue());
            }
        } else {
            apply.setApplyType(WarehouseMaterialApplyType.BARN.getValue());
        }
        doctorWarehouseMaterialApplyDao.create(apply);
    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);

        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
            recalculate(materialHandle);
        }

        doctorWarehouseMaterialApplyDao.deleteByMaterialHandle(materialHandle.getId());
    }
}
