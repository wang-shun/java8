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
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.BeanUtils;
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
    public DoctorWarehouseMaterialHandle create(WarehouseStockOutDto.WarehouseStockOutDetail detail,
                                                WarehouseStockOutDto stockDto,
                                                DoctorWarehouseStockHandle stockHandle,
                                                DoctorWareHouse wareHouse) {
        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);
        materialHandle.setType(WarehouseMaterialHandleType.OUT.getValue());

        //出库类型，当天最后一笔
        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {

            materialHandle.setHandleDate(this.buildNewHandleDate(stockDto.getHandleDate()).getTime());

            //获取该笔明细之前的库存量，包括该事件日期
            BigDecimal historyQuantity = getHistoryQuantityInclude(materialHandle.getHandleDate(), wareHouse.getId(), detail.getMaterialId());

            if (historyQuantity.compareTo(materialHandle.getQuantity()) < 0) {
//                throw new ServiceException("warehouse.stock.not.enough");
                throw new InvalidException("history.stock.not.enough.no.unit", materialHandle.getWarehouseName(), materialHandle.getMaterialName(), historyQuantity);
            }

            materialHandle.setBeforeStockQuantity(historyQuantity);
            historyQuantity = historyQuantity.subtract(detail.getQuantity());

            //该笔单据明细之后单据明细需要重算
            recalculate(materialHandle.getHandleDate(), false, wareHouse.getId(), detail.getMaterialId(), historyQuantity);
        } else {
            BigDecimal currentQuantity = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(detail.getMaterialId(), wareHouse.getId())
                    .orElse(DoctorWarehouseStock.builder().quantity(new BigDecimal(0)).build())
                    .getQuantity();

            if (currentQuantity.compareTo(materialHandle.getQuantity()) < 0)
//                throw new ServiceException("warehouse.stock.not.enough");
                throw new InvalidException("stock.not.enough.no.unit", materialHandle.getWarehouseName(), materialHandle.getMaterialName(), currentQuantity);

            materialHandle.setBeforeStockQuantity(currentQuantity);
        }
        doctorWarehouseMaterialHandleDao.create(materialHandle);
        createApply(materialHandle, detail);
        return materialHandle;
    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);

//        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
        recalculate(materialHandle);
//        }

        doctorWarehouseMaterialApplyDao.deleteByMaterialHandle(materialHandle.getId());
    }

    public DoctorWarehouseMaterialApply createApply(DoctorWarehouseMaterialHandle materialHandle, WarehouseStockOutDto.WarehouseStockOutDetail detail) {
        DoctorWarehouseMaterialApply apply = new DoctorWarehouseMaterialApply();
        apply.setMaterialHandleId(materialHandle.getId());
        apply.setType(materialHandle.getWarehouseType());
        apply.setAmount(new BigDecimal(0));
        apply.setUnitPrice(new BigDecimal(0));
        apply.setUnit(materialHandle.getUnit());
        apply.setMaterialId(materialHandle.getMaterialId());
        apply.setMaterialName(materialHandle.getMaterialName());
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
        apply.setApplyYear(materialHandle.getHandleYear());
        apply.setApplyMonth(materialHandle.getHandleMonth());
        apply.setRefundAmount(new BigDecimal(0));
        apply.setRefundQuantity(new BigDecimal(0));
        if (detail.getApplyPigGroupId() != null) {
            if (detail.getApplyPigGroupId().equals(-1L)) {
                apply.setPigGroupName("母猪");
                apply.setApplyType(WarehouseMaterialApplyType.SOW.getValue());
            } else {
                apply.setPigGroupName(detail.getApplyPigGroupName());
                apply.setApplyType(WarehouseMaterialApplyType.GROUP.getValue());
            }

            DoctorWarehouseMaterialApply barnApply = new DoctorWarehouseMaterialApply();
            BeanUtils.copyProperties(apply, barnApply);
            barnApply.setApplyType(WarehouseMaterialApplyType.BARN.getValue());
            barnApply.setPigGroupId(null);
            barnApply.setPigGroupName(null);
            doctorWarehouseMaterialApplyDao.create(barnApply);
        } else {
            apply.setApplyType(WarehouseMaterialApplyType.BARN.getValue());
        }
        doctorWarehouseMaterialApplyDao.create(apply);
        return apply;
    }
}
