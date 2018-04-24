package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockTransferDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseTransferManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/4/21.
 */
@Component
public class WarehouseTransferStockService
        extends AbstractWarehouseStockService<WarehouseStockTransferDto, WarehouseStockTransferDto.WarehouseStockTransferDetail> {

    @Autowired
    private WarehouseTransferManager warehouseTransferManager;

    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<WarehouseStockTransferDto.WarehouseStockTransferDetail> getDetails(WarehouseStockTransferDto stockDto) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void create(WarehouseStockTransferDto stockDto, WarehouseStockTransferDto.WarehouseStockTransferDetail detail, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeUpdate(WarehouseStockTransferDto stockDto, DoctorWarehouseStockHandle stockHandle) {
        if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue()))
            throw new ServiceException("transfer.in.not.allow.edit");
    }

    @Override
    protected DoctorWarehouseStockHandle create(WarehouseStockTransferDto stockDto, DoctorWareHouse wareHouse) {

        //调出单据
        DoctorWarehouseStockHandle transferOutStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.TRANSFER_OUT, null);

        //按照调入仓库分组，生成多个调入单据
        Map<Long/*warehouseId*/, List<WarehouseStockTransferDto.WarehouseStockTransferDetail>> eachWarehouseTransferIn = stockDto.getDetails()
                .stream()
                .collect(Collectors.groupingBy(WarehouseStockTransferDto.WarehouseStockTransferDetail::getTransferInWarehouseId));

        eachWarehouseTransferIn.forEach((w, details) -> {
            //调入仓库
            DoctorWareHouse transferInWarehouse = doctorWareHouseDao.findById(w);
            //调入单据
            DoctorWarehouseStockHandle transferInStockHandle = doctorWarehouseStockHandleManager.create(stockDto, transferInWarehouse, WarehouseMaterialHandleType.TRANSFER_IN, transferOutStockHandle.getId());

            details.forEach(detail -> {
                DoctorWarehouseMaterialHandle out = warehouseTransferManager.create(detail, stockDto, transferOutStockHandle, wareHouse);
                doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity(), wareHouse);

                DoctorWarehouseMaterialHandle in = warehouseTransferManager.create(detail, stockDto, transferInStockHandle, transferInWarehouse);
                doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), transferInWarehouse);

                out.setRelMaterialHandleId(in.getId());
                in.setRelMaterialHandleId(out.getId());
                doctorWarehouseMaterialHandleDao.update(out);
                doctorWarehouseMaterialHandleDao.update(in);
            });
        });

        return transferOutStockHandle;
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {

        DoctorWarehouseMaterialHandle transferIn = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());
        warehouseTransferManager.delete(transferIn);
        warehouseTransferManager.delete(materialHandle);

        DoctorWareHouse transferOutWareHouse = new DoctorWareHouse();
        transferOutWareHouse.setId(materialHandle.getWarehouseId());
        transferOutWareHouse.setWareHouseName(materialHandle.getWarehouseName());
        doctorWarehouseStockManager.in(materialHandle.getMaterialId(), materialHandle.getQuantity(), transferOutWareHouse);

        DoctorWareHouse transferInWareHouse = new DoctorWareHouse();
        transferInWareHouse.setId(transferIn.getWarehouseId());
        transferInWareHouse.setWareHouseName(transferIn.getWarehouseName());
        doctorWarehouseStockManager.out(transferIn.getMaterialId(), transferIn.getQuantity(), transferInWareHouse);
    }

    @Override
    public void changed(Map<WarehouseStockTransferDto.WarehouseStockTransferDetail, DoctorWarehouseMaterialHandle> changed, DoctorWarehouseStockHandle stockHandle, WarehouseStockTransferDto stockDto, DoctorWareHouse wareHouse) {

        Map<Long/*transferInMaterialHandleId*/, DoctorWarehouseStockHandle> newTransferInStockHandle = new HashMap<>();
        Map<Long/*transferInMaterialHandleId*/, DoctorWareHouse> oldTransferInWarehouse = new HashMap<>();
        changed.forEach((detail, materialHandle) -> {
            DoctorWarehouseMaterialHandle transferIn = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());
            if (!detail.getTransferInWarehouseId().equals(transferIn.getWarehouseId())) {
                DoctorWareHouse newTransferInWarehouse = doctorWareHouseDao.findById(detail.getTransferInWarehouseId());
                newTransferInStockHandle.put(detail.getTransferInWarehouseId(), doctorWarehouseStockHandleManager.create(stockDto, newTransferInWarehouse, WarehouseMaterialHandleType.TRANSFER_IN, null));
                oldTransferInWarehouse.put(detail.getTransferInWarehouseId(), newTransferInWarehouse);
            }
        });


        changed.forEach((detail, materialHandle) -> {

            DoctorWarehouseMaterialHandle transferIn = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());

            boolean changedTransferInWarehouse = !detail.getTransferInWarehouseId().equals(transferIn.getWarehouseId());

            materialHandle.setRemark(detail.getRemark());
            if (!changedTransferInWarehouse)
                transferIn.setRemark(detail.getRemark());

            if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0
                    || !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime())
                    || changedTransferInWarehouse) {

                DoctorWareHouse transferInWarehouse = doctorWareHouseDao.findById(transferIn.getWarehouseId());

                if (changedTransferInWarehouse) {
                    //原调入仓库扣减库存
                    doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), transferInWarehouse);
                    //删除原调入明细
                    warehouseTransferManager.delete(transferIn);

                    //新的调入仓库
                    //TODO 锁
                    transferInWarehouse = oldTransferInWarehouse.get(detail.getTransferInWarehouseId());
                    //创建新的调入明细
                    warehouseTransferManager.create(detail, stockDto, newTransferInStockHandle.get(detail.getTransferInWarehouseId()), transferInWarehouse);
                    //新调入仓库增加库存
                    doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), transferInWarehouse);
                }

                //更改了数量，
                if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0) {
                    BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
                    if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {
                        doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity, wareHouse);
                        if (!changedTransferInWarehouse)
                            doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity, transferInWarehouse);
                    } else {
                        doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity.negate(), wareHouse);
                        if (!changedTransferInWarehouse)
                            doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity.negate(), transferInWarehouse);
                    }
                    materialHandle.setQuantity(detail.getQuantity());
                    if (!changedTransferInWarehouse)
                        transferIn.setQuantity(detail.getQuantity());
                }


                Date recalculateDate = materialHandle.getHandleDate();
                int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
                //更改了操作日期
                if (days != 0) {
                    warehouseTransferManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());
                    if (!changedTransferInWarehouse) {
                        transferIn.setHandleDate(materialHandle.getHandleDate());
                        transferIn.setHandleYear(materialHandle.getHandleYear());
                        transferIn.setHandleMonth(materialHandle.getHandleMonth());
                        doctorWarehouseMaterialHandleDao.update(transferIn);
                    }

                    if (days < 0) {//事件日期改小了，重算日期采用新的日期
                        recalculateDate = materialHandle.getHandleDate();
                    }
                }

                doctorWarehouseMaterialHandleDao.update(materialHandle);
                warehouseTransferManager.recalculate(materialHandle, recalculateDate);
                if (!changedTransferInWarehouse)
                    warehouseTransferManager.recalculate(transferIn, recalculateDate);
            } else {
                //只更新了备注
                doctorWarehouseMaterialHandleDao.update(materialHandle);
                if (!changedTransferInWarehouse)
                    doctorWarehouseMaterialHandleDao.update(transferIn);
            }
        });
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle,
                           WarehouseStockTransferDto.WarehouseStockTransferDetail detail,
                           DoctorWarehouseStockHandle stockHandle,
                           WarehouseStockTransferDto stockDto,
                           DoctorWareHouse wareHouse) {
        throw new UnsupportedOperationException();
    }
}
