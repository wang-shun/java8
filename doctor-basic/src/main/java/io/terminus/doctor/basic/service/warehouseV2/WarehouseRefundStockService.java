package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockRefundDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseReturnManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/4/20.
 */
@Component
public class WarehouseRefundStockService extends AbstractWarehouseStockService<WarehouseStockRefundDto, WarehouseStockRefundDto.WarehouseStockRefundDetailDto> {

    @Autowired
    private WarehouseReturnManager warehouseReturnManager;

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;


    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        return WarehouseMaterialHandleType.RETURN;
    }

    @Override
    protected List<WarehouseStockRefundDto.WarehouseStockRefundDetailDto> getDetails(WarehouseStockRefundDto stockDto) {
        return stockDto.getDetails();
    }


    @Override
    protected DoctorWarehouseStockHandle create(WarehouseStockRefundDto stockDto, DoctorWareHouse wareHouse) {

        //退料对应的出库单据
        DoctorWarehouseStockHandle outStockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getOutStockHandleId());

        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, getMaterialHandleType(), outStockHandle.getId());
        warehouseReturnManager.create(stockDto.getDetails(), stockDto, stockHandle, wareHouse);

        stockDto.getDetails().forEach(detail -> {
            doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity().multiply(BigDecimal.valueOf(-1)), wareHouse);
        });

        return stockHandle;
    }

    @Override
    protected void create(WarehouseStockRefundDto stockDto, WarehouseStockRefundDto.WarehouseStockRefundDetailDto detail, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {
        warehouseReturnManager.delete(materialHandle);

        DoctorWareHouse wareHouse = new DoctorWareHouse();
        wareHouse.setId(materialHandle.getWarehouseId());
        wareHouse.setWareHouseName(materialHandle.getMaterialName());
        doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
    }

    @Override
    public void beforeUpdate(WarehouseStockRefundDto stockDto, DoctorWarehouseStockHandle stockHandle) {
//        if (!stockDto.getOutStockHandleId().equals(stockHandle.getRelStockHandleId()))
//            throw new ServiceException("refund.out.stock.handle.not.allow.change");
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle, WarehouseStockRefundDto.WarehouseStockRefundDetailDto detail, DoctorWarehouseStockHandle stockHandle, WarehouseStockRefundDto stockDto, DoctorWareHouse wareHouse) {

        materialHandle.setRemark(detail.getRemark());
        materialHandle.setSettlementDate(stockDto.getSettlementDate());

        DoctorWarehouseMaterialHandle outMaterialHandle = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());

        boolean changeHandleDate = !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());

        if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0
                || changeHandleDate) {

            //更改了数量，或更改了操作日期
            if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0) {
                //可退数量
                BigDecimal alreadyRefundQuantity = doctorWarehouseMaterialHandleDao.countQuantityAlreadyRefund(materialHandle.getRelMaterialHandleId());
                if (outMaterialHandle.getQuantity().add(alreadyRefundQuantity).compareTo((detail.getQuantity().multiply(BigDecimal.valueOf(-1)))) < 0)
                    throw new InvalidException("quantity.not.enough.to.refund", outMaterialHandle.getQuantity().add(alreadyRefundQuantity));

                BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
                if (changedQuantity.compareTo(new BigDecimal(0)) < 0) {//退料数量改大
                    doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity.multiply(BigDecimal.valueOf(-1)), wareHouse);
                } else {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity.multiply(BigDecimal.valueOf(-1)).negate(), wareHouse);
                }
                materialHandle.setQuantity(detail.getQuantity());

//                DoctorWarehouseMaterialApply apply1 = doctorWarehouseMaterialApplyDao.findMaterialHandle(materialHandle.getRelMaterialHandleId());
//                apply1.setRefundQuantity(apply1.getRefundQuantity().add(materialHandle.getQuantity().subtract(outMaterialHandle.getQuantity())));
//                doctorWarehouseMaterialApplyDao.update(apply1);

                //更新领用记录中的退料数量
                doctorWarehouseMaterialApplyDao.findAllByMaterialHandle(materialHandle.getRelMaterialHandleId()).forEach(apply -> {
                    if (apply.getRefundQuantity() == null)
                        apply.setRefundQuantity(new BigDecimal(0));

                    apply.setRefundQuantity(apply.getRefundQuantity().add(changedQuantity));
                    doctorWarehouseMaterialApplyDao.update(apply);
                });
            }

            Date recalculateDate = materialHandle.getHandleDate();
            if (changeHandleDate) {

                warehouseReturnManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());

                if (outMaterialHandle.getHandleDate().after(materialHandle.getHandleDate()) &&
                        !outMaterialHandle.getHandleDate().equals(materialHandle.getHandleDate()))
                    throw new ServiceException("refund.date.before.out.date");

                if (stockDto.getHandleDate().getTime().before(stockHandle.getHandleDate())) {//事件日期改小了，重算日期采用新的日期
                    recalculateDate = materialHandle.getHandleDate();
                }
            }
            doctorWarehouseMaterialHandleDao.update(materialHandle);
            warehouseReturnManager.recalculate(materialHandle, recalculateDate);

        } else {
            //只更新了备注
            doctorWarehouseMaterialHandleDao.update(materialHandle);
        }
    }

}
