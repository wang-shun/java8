package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockOutDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialApplyType;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseOutManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/4/19.
 */
@Service
public class WarehouseOutStockService extends AbstractWarehouseStockService<WarehouseStockOutDto, WarehouseStockOutDto.WarehouseStockOutDetail> {

    @Autowired
    private WarehouseOutManager warehouseOutManager;

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        return WarehouseMaterialHandleType.OUT;
    }

    @Override
    protected List<WarehouseStockOutDto.WarehouseStockOutDetail> getDetails(WarehouseStockOutDto stockDto) {
        return stockDto.getDetails();
    }

    @Override
    protected void create(WarehouseStockOutDto stockDto,
                          WarehouseStockOutDto.WarehouseStockOutDetail detail,
                          DoctorWarehouseStockHandle stockHandle,
                          DoctorWareHouse wareHouse) {

        warehouseOutManager.create(detail, stockDto, stockHandle, wareHouse);
        doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity(), wareHouse);
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {
        warehouseOutManager.delete(materialHandle);

        DoctorWareHouse wareHouse = new DoctorWareHouse();
        wareHouse.setId(materialHandle.getWarehouseId());
        wareHouse.setWareHouseName(materialHandle.getWarehouseName());
        wareHouse.setFarmId(materialHandle.getFarmId());
        wareHouse.setType(materialHandle.getWarehouseType());
        doctorWarehouseStockManager.in(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle,
                           WarehouseStockOutDto.WarehouseStockOutDetail detail,
                           DoctorWarehouseStockHandle stockHandle,
                           WarehouseStockOutDto stockDto,
                           DoctorWareHouse wareHouse) {

        materialHandle.setRemark(detail.getRemark());

        DoctorWarehouseMaterialApply apply = doctorWarehouseMaterialApplyDao.findMaterialHandle(materialHandle.getId());
        boolean applyChanged = false;
        boolean changeHandleDate = !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());

        if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0
                || changeHandleDate) {

            //更改了数量，或更改了操作日期

            if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0) {
                BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
                if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity, wareHouse);
                } else {
                    doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity.negate(), wareHouse);
                }
                materialHandle.setQuantity(detail.getQuantity());
            }
            Date recalculateDate = materialHandle.getHandleDate();
            int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
            if (days != 0) {
                warehouseOutManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());
                doctorWarehouseMaterialHandleDao.update(materialHandle);
                if (days < 0) {//事件日期改小了，重算日期采用新的日期
                    recalculateDate = materialHandle.getHandleDate();
                }
                apply.setApplyDate(materialHandle.getHandleDate());
                apply.setApplyYear(stockDto.getHandleDate().get(Calendar.YEAR));
                apply.setApplyMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
                applyChanged = true;
            }
            warehouseOutManager.recalculate(materialHandle, recalculateDate);

        } else if (!detail.getApplyPigBarnId().equals(apply.getPigBarnId())
                || (detail.getApplyPigGroupId() != null && !detail.getApplyPigGroupId().equals(apply.getPigGroupId()))) {
            apply.setPigBarnId(detail.getApplyPigBarnId());
            apply.setPigGroupId(detail.getApplyPigBarnId());
            apply.setApplyStaffId(detail.getApplyStaffId());
            apply.setApplyStaffName(detail.getApplyStaffName());
            apply.setPigType(detail.getPigType());

            if (apply.getApplyType().equals(WarehouseMaterialApplyType.BARN.getValue())
                    && (detail.getApplyPigGroupId().equals(-1) || detail.getApplyPigGroupId() != null)) {

                DoctorWarehouseMaterialApply groupApply = new DoctorWarehouseMaterialApply();

                if (detail.getApplyPigGroupId().equals(-1)) {
                    groupApply.setApplyType(WarehouseMaterialApplyType.SOW.getValue());
                    groupApply.setPigGroupId(-1L);
                    groupApply.setPigGroupName("母猪");
                }
                if (detail.getApplyPigGroupId() != null) {
                    groupApply.setApplyType(WarehouseMaterialApplyType.GROUP.getValue());
                }
                //新增猪群领用
                doctorWarehouseMaterialApplyDao.create(groupApply);
            } else if ((apply.getApplyType().equals(WarehouseMaterialApplyType.GROUP.getValue()) || apply.getApplyType().equals(WarehouseMaterialApplyType.SOW.getValue()))
                    && detail.getApplyPigGroupId() == null) {
                //删除原母猪或猪群领用
                doctorWarehouseMaterialApplyDao.deleteByMaterialHandle(materialHandle.getId());
            } else if ((apply.getApplyType().equals(WarehouseMaterialApplyType.SOW.getValue()) && detail.getApplyPigGroupId() != -1)
                    || (apply.getApplyType().equals(WarehouseMaterialApplyType.GROUP.getValue()) && detail.getApplyPigGroupId() == -1)) {
                //从猪群改成母猪，从母猪改成猪群
                if (detail.getApplyPigGroupId().equals(-1)) {
                    apply.setApplyType(WarehouseMaterialApplyType.SOW.getValue());
                    apply.setPigGroupName("母猪");
                    apply.setPigGroupId(-1L);
                } else {
                    apply.setApplyType(WarehouseMaterialApplyType.GROUP.getValue());
                    apply.setPigGroupName(detail.getApplyPigGroupName());
                    apply.setPigGroupId(detail.getApplyPigGroupId());
                }
            }
            applyChanged = true;
        } else {
            //只更新了备注
            doctorWarehouseMaterialHandleDao.update(materialHandle);
        }
        if (applyChanged)
            doctorWarehouseMaterialApplyDao.update(apply);
    }

}
