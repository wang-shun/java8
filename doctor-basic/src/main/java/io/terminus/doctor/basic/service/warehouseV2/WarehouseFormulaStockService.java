package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseFormulaDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseFormulaManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/4/21.
 */
@Component
public class WarehouseFormulaStockService extends AbstractWarehouseStockService<WarehouseFormulaDto, WarehouseFormulaDto.WarehouseFormulaDetail> {


    @Autowired
    private WarehouseFormulaManager warehouseFormulaManager;

    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        return null;
    }

    @Override
    protected List<WarehouseFormulaDto.WarehouseFormulaDetail> getDetails(WarehouseFormulaDto stockDto) {
        return null;
    }

    @Override
    protected void create(WarehouseFormulaDto stockDto,
                          WarehouseFormulaDto.WarehouseFormulaDetail detail,
                          DoctorWarehouseStockHandle stockHandle,
                          DoctorWareHouse wareHouse) {
        //由下方create替代
        throw new UnsupportedOperationException();
    }

    @Override
    protected DoctorWarehouseStockHandle create(WarehouseFormulaDto stockDto, DoctorWareHouse wareHouse) {

        DoctorWarehouseStockHandle inStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.FORMULA_IN, null);

        WarehouseFormulaDto.WarehouseFormulaDetail inDetail = new WarehouseFormulaDto.WarehouseFormulaDetail();
        inDetail.setQuantity(stockDto.getFeedMaterialQuantity());
        inDetail.setWarehouseId(stockDto.getWarehouseId());
        inDetail.setMaterialId(stockDto.getFeedMaterialId());
        //创建配方生产入库明细单
        DoctorWarehouseMaterialHandle inMaterialHandle = warehouseFormulaManager.create(inDetail, stockDto, inStockHandle, wareHouse);
        //增加库存
        doctorWarehouseStockManager.in(stockDto.getFeedMaterialId(), stockDto.getFeedMaterialQuantity(), wareHouse);

        //每个出库仓库及对应的单据
        Map<Long/**warehouseId*/, DoctorWarehouseStockHandle> eachWarehouseStockHandles = new HashMap<>();
        Map<Long/*warehouseId*/, DoctorWareHouse> outWarehouses = new HashMap<>();
        stockDto.getDetails()
                .stream()
                .map(WarehouseFormulaDto.WarehouseFormulaDetail::getWarehouseId)
                .collect(Collectors.toSet()).forEach(w -> {

            DoctorWareHouse outWarehouse = doctorWareHouseDao.findById(w);
            eachWarehouseStockHandles.put(w, doctorWarehouseStockHandleManager.create(stockDto, outWarehouse, WarehouseMaterialHandleType.FORMULA_OUT, inStockHandle.getId()));
            outWarehouses.put(w, outWarehouse);
        });

        stockDto.getDetails().forEach(detail -> {

            //创建配方生产出库明细单
            DoctorWarehouseMaterialHandle outMaterialHandle = warehouseFormulaManager.create(detail, stockDto, eachWarehouseStockHandles.get(detail.getWarehouseId()), outWarehouses.get(detail.getWarehouseId()));
            outMaterialHandle.setRelMaterialHandleId(inMaterialHandle.getId());
            doctorWarehouseMaterialHandleDao.update(outMaterialHandle);
            //扣减库存
            doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity(), outWarehouses.get(detail.getWarehouseId()));
        });


        return inStockHandle;
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {
        //配方生产出库不允许删除明细
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeUpdate(WarehouseFormulaDto stockDto, DoctorWarehouseStockHandle stockHandle) {
        if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue()))
            throw new ServiceException("formula.in.not.allow.edit");
    }

    @Override
    protected DoctorWarehouseStockHandle update(WarehouseFormulaDto stockDto,
                                                DoctorWareHouse wareHouse,
                                                DoctorWarehouseStockHandle stockHandle) {

        boolean changeHandleDate = !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());

        List<DoctorWarehouseMaterialHandle> inMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandle(stockHandle.getId());
        if (inMaterialHandles.isEmpty())
            throw new ServiceException("");
        DoctorWarehouseMaterialHandle inMaterialHandle = inMaterialHandles.get(0);

        if (!stockHandle.getWarehouseId().equals(stockDto.getWarehouseId())) {
            //更换了入库仓库
            DoctorWareHouse oldInWarehouse = new DoctorWareHouse();
            oldInWarehouse.setId(inMaterialHandle.getWarehouseId());
            oldInWarehouse.setWareHouseName(inMaterialHandle.getWarehouseName());
            doctorWarehouseStockManager.out(stockDto.getFeedMaterialId(), stockDto.getFeedMaterialQuantity(), oldInWarehouse);
            warehouseFormulaManager.delete(inMaterialHandle);

            WarehouseFormulaDto.WarehouseFormulaDetail detail = new WarehouseFormulaDto.WarehouseFormulaDetail();
            detail.setMaterialId(stockDto.getFeedMaterialId());
            detail.setWarehouseId(stockDto.getWarehouseId());
            detail.setQuantity(stockDto.getFeedMaterialQuantity());
            warehouseFormulaManager.create(detail, stockDto, stockHandle, wareHouse);
            doctorWarehouseStockManager.in(stockDto.getFeedMaterialId(), stockDto.getFeedMaterialQuantity(), wareHouse);

        } else {
            if (!inMaterialHandle.getQuantity().equals(stockDto.getFeedMaterialQuantity())
                    || changeHandleDate) {

                if (!inMaterialHandle.getQuantity().equals(stockDto.getFeedMaterialQuantity())) {
                    //更改了饲料入库数量
                    if (inMaterialHandle.getQuantity().compareTo(stockDto.getFeedMaterialQuantity()) > 0)
                        doctorWarehouseStockManager.out(stockDto.getFeedMaterialId(), inMaterialHandle.getQuantity().subtract(stockDto.getFeedMaterialQuantity()), wareHouse);
                    else
                        doctorWarehouseStockManager.in(stockDto.getFeedMaterialId(), stockDto.getFeedMaterialQuantity().subtract(inMaterialHandle.getQuantity()), wareHouse);
                }

                inMaterialHandle.setQuantity(stockDto.getFeedMaterialQuantity());

                Date recalculateDate = stockHandle.getHandleDate();

                if (changeHandleDate) {

                    warehouseFormulaManager.buildNewHandleDateForUpdate(stockHandle, stockDto.getHandleDate());
                    doctorWarehouseStockHandleDao.update(stockHandle);

                    warehouseFormulaManager.buildNewHandleDateForUpdate(inMaterialHandle, stockDto.getHandleDate());
                    doctorWarehouseMaterialHandleDao.update(inMaterialHandle);

                    int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
                    if (days < 0) {//事件日期改小了，重算日期采用新的日期
                        recalculateDate = inMaterialHandle.getHandleDate();
                    }
                }

                warehouseFormulaManager.recalculate(inMaterialHandle, recalculateDate);
            }
        }

        //出库明细修改
        List<DoctorWarehouseStockHandle> outStockHandles = doctorWarehouseStockHandleDao.findRelStockHandle(stockHandle.getId());
        Map<Long, DoctorWarehouseMaterialHandle> outMaterialHandles = new HashMap<>();
        outStockHandles.forEach(sh -> {
            doctorWarehouseMaterialHandleDao.findByStockHandle(sh.getId()).forEach(m -> {
                outMaterialHandles.put(m.getId(), m);
            });
        });
        if (changeHandleDate) {
            outStockHandles.forEach(sh -> {
                warehouseFormulaManager.buildNewHandleDateForUpdate(sh, stockDto.getHandleDate());
                doctorWarehouseStockHandleDao.update(sh);
            });
        }
        stockDto.getDetails().forEach(detail -> {

            DoctorWarehouseMaterialHandle outMaterialHandle = outMaterialHandles.get(detail.getMaterialHandleId());
            outMaterialHandle.setRemark(detail.getRemark());
            if (!detail.getQuantity().equals(outMaterialHandle.getQuantity())
                    || changeHandleDate) {

                DoctorWareHouse outWarehouse = new DoctorWareHouse();
                outWarehouse.setId(outMaterialHandle.getWarehouseId());
                outWarehouse.setWareHouseName(outMaterialHandle.getWarehouseName());

                if (detail.getQuantity().compareTo(outMaterialHandle.getQuantity()) > 0) {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity().subtract(outMaterialHandle.getQuantity()), outWarehouse);
                } else {
                    doctorWarehouseStockManager.in(detail.getMaterialId(), outMaterialHandle.getQuantity().subtract(detail.getQuantity()), outWarehouse);
                }

                outMaterialHandle.setQuantity(detail.getQuantity());

                Date recalculateDate = stockHandle.getHandleDate();

                if (changeHandleDate) {
                    warehouseFormulaManager.buildNewHandleDateForUpdate(outMaterialHandle, stockDto.getHandleDate());
                    doctorWarehouseMaterialHandleDao.update(outMaterialHandle);

                    int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
                    if (days < 0) {//事件日期改小了，重算日期采用新的日期
                        recalculateDate = outMaterialHandle.getHandleDate();
                    }
                }

                warehouseFormulaManager.recalculate(outMaterialHandle, recalculateDate);
            } else {
                doctorWarehouseMaterialHandleDao.update(outMaterialHandle);
            }
        });

        return stockHandle;
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle,
                           WarehouseFormulaDto.WarehouseFormulaDetail detail,
                           DoctorWarehouseStockHandle stockHandle,
                           WarehouseFormulaDto stockDto,
                           DoctorWareHouse wareHouse) {
        throw new UnsupportedOperationException();
    }
}
