package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.ServiceException;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void changed(Map<WarehouseStockOutDto.WarehouseStockOutDetail, DoctorWarehouseMaterialHandle> changed, DoctorWarehouseStockHandle stockHandle, WarehouseStockOutDto stockDto, DoctorWareHouse wareHouse) {

        if (!DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime())) {
            Date newDate = warehouseOutManager.buildNewHandleDate(stockDto.getHandleDate()).getTime();

            //最早一笔退料入库明细单据的事件日期
            Date firstRefundDate = doctorWarehouseMaterialHandleDao.findFirstRefundHandleDate(changed.values().stream().map(DoctorWarehouseMaterialHandle::getId).collect(Collectors.toList()));

            if (firstRefundDate != null && newDate.after(firstRefundDate)) {
                throw new ServiceException("out.handle.date.after.refund");
            }
        }

        changed.forEach((detail, materialHandle) -> {
            this.changed(materialHandle, detail, stockHandle, stockDto, wareHouse);
        });
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle,
                           WarehouseStockOutDto.WarehouseStockOutDetail detail,
                           DoctorWarehouseStockHandle stockHandle,
                           WarehouseStockOutDto stockDto,
                           DoctorWareHouse wareHouse) {

        materialHandle.setRemark(detail.getRemark());
        materialHandle.setSettlementDate(stockDto.getSettlementDate());

        boolean changeHandleDate = !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
        boolean changeQuantity = detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0;

        if (changeQuantity || changeHandleDate) {

            //更改了数量，或更改了操作日期

            if (changeQuantity) {

                BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
                if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity, wareHouse);
                } else {
                    //已退数量
                    BigDecimal alreadyRefundQuantity = doctorWarehouseMaterialHandleDao.countQuantityAlreadyRefund(materialHandle.getId());
                    if (detail.getQuantity().compareTo(alreadyRefundQuantity) < 0)
                        throw new ServiceException("warehouse.stock.not.enough");

                    doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity.negate(), wareHouse);
                }
                materialHandle.setQuantity(detail.getQuantity());
            }
            Date recalculateDate = materialHandle.getHandleDate();
            if (changeHandleDate) {
                warehouseOutManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());
                if (stockDto.getHandleDate().getTime().before(stockHandle.getHandleDate())) {//事件日期改早了，重算日期采用新的日期
                    recalculateDate = materialHandle.getHandleDate();
                }
            }
            doctorWarehouseMaterialHandleDao.update(materialHandle);
            warehouseOutManager.recalculate(materialHandle, recalculateDate);

        } else {
            //只更新了备注
            doctorWarehouseMaterialHandleDao.update(materialHandle);
        }

        DoctorWarehouseMaterialApply apply = doctorWarehouseMaterialApplyDao.findMaterialHandle(materialHandle.getId());
        if (null == apply)
            warehouseOutManager.createApply(materialHandle, detail);
        else if (!detail.getApplyPigBarnId().equals(apply.getPigBarnId())
                || (detail.getApplyPigGroupId() != null && !detail.getApplyPigGroupId().equals(apply.getPigGroupId()))
                || (detail.getApplyPigGroupId() == null && apply.getPigGroupId() != null)
                || changeHandleDate
                || changeQuantity) {

            if (changeHandleDate) {
                apply.setSettlementDate(stockDto.getSettlementDate());
                apply.setSettlementDate(stockDto.getSettlementDate());
                apply.setApplyDate(stockDto.getHandleDate().getTime());
                apply.setApplyYear(stockDto.getHandleDate().get(Calendar.YEAR));
                apply.setApplyMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
            }

            if (changeQuantity) {
                apply.setQuantity(detail.getQuantity());
            }

            apply.setPigBarnId(detail.getApplyPigBarnId());
            apply.setPigBarnName(detail.getApplyPigBarnName());
            apply.setApplyStaffId(detail.getApplyStaffId());
            apply.setApplyStaffName(detail.getApplyStaffName());
            apply.setPigType(detail.getPigType());

            if (apply.getApplyType().equals(WarehouseMaterialApplyType.SOW.getValue())
                    || apply.getApplyType().equals(WarehouseMaterialApplyType.GROUP.getValue())) {

                if (detail.getApplyPigGroupId() == null) {
                    //从母猪或猪群领用调整为猪舍领用，删除猪群或母猪领用
                    doctorWarehouseMaterialApplyDao.deleteGroupApply(materialHandle.getId());
                } else if (apply.getApplyType().equals(WarehouseMaterialApplyType.SOW.getValue()) && !detail.getApplyPigGroupId().equals(-1L)) {
                    //从母猪领用调整为猪群领用
                    apply.setApplyType(WarehouseMaterialApplyType.GROUP.getValue());
                    apply.setPigGroupName(detail.getApplyPigGroupName());
                    apply.setPigGroupId(detail.getApplyPigGroupId());
                } else if (apply.getApplyType().equals(WarehouseMaterialApplyType.GROUP.getValue()) && detail.getApplyPigGroupId().equals(-1L)) {
                    //从猪群领用调整为母猪领用
                    apply.setApplyType(WarehouseMaterialApplyType.SOW.getValue());
                    apply.setPigGroupName("母猪");
                    apply.setPigGroupId(-1L);
                }
                //还需要调整猪舍领用
                doctorWarehouseMaterialApplyDao.updateBarnApply(materialHandle.getId(), apply);
            } else {
                if (detail.getApplyPigGroupId() != null) {
                    //原猪舍改为猪群或母猪领用，需补一条
                    DoctorWarehouseMaterialApply groupApply = new DoctorWarehouseMaterialApply();
                    BeanUtils.copyProperties(apply, groupApply);
                    if (detail.getApplyPigGroupId().equals(-1L)) {
                        groupApply.setApplyType(WarehouseMaterialApplyType.SOW.getValue());
                        groupApply.setPigGroupId(-1L);
                        groupApply.setPigGroupName("母猪");
                    } else {
                        groupApply.setApplyType(WarehouseMaterialApplyType.GROUP.getValue());
                        groupApply.setPigGroupId(detail.getApplyPigGroupId());
                        groupApply.setPigGroupName(detail.getApplyPigGroupName());
                    }
                    //新增猪群领用
                    doctorWarehouseMaterialApplyDao.create(groupApply);
                }
            }

            doctorWarehouseMaterialApplyDao.update(apply);
        }
    }
}
