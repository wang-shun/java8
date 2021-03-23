package io.terminus.doctor.basic.service.warehouseV2;

import com.sun.org.apache.regexp.internal.RE;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseFormulaDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseFormulaManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;
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
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<WarehouseFormulaDto.WarehouseFormulaDetail> getDetails(WarehouseFormulaDto stockDto) {
//        throw new UnsupportedOperationException();
        return stockDto.getDetails();
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

        // 配方是否有物料已盘点 （陈娟 2018-09-27）
        DoctorWarehouseMaterialHandle material;
        String str = new String();
        // 入库单据是否盘点
        material = doctorWarehouseMaterialHandleDao.getMaxInventoryDate(stockDto.getWarehouseId(), stockDto.getFeedMaterialId(), stockDto.getHandleDate().getTime());
        if(material!=null){
            str = str + material.getMaterialName() + ",";
        }
        // 出库单据是否盘点
        List<WarehouseFormulaDto.WarehouseFormulaDetail> details = stockDto.getDetails();
        for (WarehouseFormulaDto.WarehouseFormulaDetail dd: details) {
            material = doctorWarehouseMaterialHandleDao.getMaxInventoryDate(dd.getWarehouseId(), dd.getMaterialId(), stockDto.getHandleDate().getTime());
            if(material!=null){
                str = str + material.getMaterialName() + ",";
            }
        }

        if(!str.equals("")){
            str = str+ "【已盘点,不可新增配方】";
            throw new ServiceException(str);
        }

        DoctorWarehouseStockHandle inStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.FORMULA_IN, null);

        WarehouseFormulaDto.WarehouseFormulaDetail inDetail = new WarehouseFormulaDto.WarehouseFormulaDetail();
        inDetail.setQuantity(stockDto.getFeedMaterialQuantity());
        inDetail.setWarehouseId(stockDto.getWarehouseId());
        inDetail.setMaterialId(stockDto.getFeedMaterialId());
        //创建配方生产入库明细单
        DoctorWarehouseMaterialHandle inMaterialHandle = warehouseFormulaManager.create(inDetail, stockDto, inStockHandle, wareHouse);
        //增加库存
        doctorWarehouseStockManager.in(stockDto.getFeedMaterialId(), stockDto.getFeedMaterialQuantity(), wareHouse);

        //按照不同的出库仓库，拆分成不同的单据
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

            //出库仓库
            DoctorWareHouse outWarehouse = outWarehouses.get(detail.getWarehouseId());

            if (null == outWarehouse)
                throw new ServiceException("warehouse.not.found");

            //创建配方生产出库明细单
            DoctorWarehouseMaterialHandle outMaterialHandle = warehouseFormulaManager.create(detail, stockDto, eachWarehouseStockHandles.get(detail.getWarehouseId()), outWarehouse);
            outMaterialHandle.setRelMaterialHandleId(inMaterialHandle.getId());
            doctorWarehouseMaterialHandleDao.update(outMaterialHandle);
            //扣减库存
            doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity(), outWarehouse);
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

    /**
     * 更改数量，更改事件日期，更改入库仓库
     *
     * @param stockDto
     * @param wareHouse   入库仓库
     * @param stockHandle 出库单据
     * @return
     */
    @Override
    protected DoctorWarehouseStockHandle update(WarehouseFormulaDto stockDto,
                                                DoctorWareHouse wareHouse,
                                                DoctorWarehouseStockHandle stockHandle) {

        //配方生产入库单据
        DoctorWarehouseStockHandle inStockHandle = doctorWarehouseStockHandleDao.findById(stockHandle.getRelStockHandleId());

        boolean changeHandleDate = !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
        boolean changeInWarehouse = !stockDto.getWarehouseId().equals(inStockHandle.getWarehouseId());

        //该出库单据下的出库明细
        Map<Long, List<DoctorWarehouseMaterialHandle>> materialHandles = doctorWarehouseMaterialHandleDao
                .findByStockHandle(stockHandle.getId())
                .stream()
                .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getId));

        BigDecimal thisStockHandleChangedQuantity = new BigDecimal(0);
        for (WarehouseFormulaDto.WarehouseFormulaDetail detail : stockDto.getDetails()) {
            if (!materialHandles.containsKey(detail.getMaterialHandleId()))
                throw new ServiceException("material.handle.unknown");
            DoctorWarehouseMaterialHandle materialHandle = materialHandles.get(detail.getMaterialHandleId()).get(0);

            materialHandle.setRemark(detail.getRemark());
            materialHandle.setSettlementDate(stockDto.getSettlementDate());

            int quantityChange = materialHandle.getQuantity().compareTo(detail.getQuantity());
            if (quantityChange != 0 || changeHandleDate) {

                DoctorWareHouse outWarehouse = new DoctorWareHouse();
                outWarehouse.setId(materialHandle.getWarehouseId());
                outWarehouse.setFarmId(materialHandle.getFarmId());
                outWarehouse.setWareHouseName(materialHandle.getWarehouseName());
                outWarehouse.setType(materialHandle.getWarehouseType());

                thisStockHandleChangedQuantity = thisStockHandleChangedQuantity.add(detail.getQuantity().subtract(materialHandle.getQuantity()));
                if (quantityChange > 0) {
                    //改小
                    doctorWarehouseStockManager.in(materialHandle.getMaterialId(), materialHandle.getQuantity().subtract(detail.getQuantity()), outWarehouse);
                } else {
                    //改大
                    doctorWarehouseStockManager.out(materialHandle.getMaterialId(), detail.getQuantity().subtract(materialHandle.getQuantity()), outWarehouse);
                }

                materialHandle.setQuantity(detail.getQuantity());

                Date recalculateDate = stockHandle.getHandleDate();
                if (changeHandleDate) {
                    warehouseFormulaManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());

                    int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
                    if (stockDto.getHandleDate().getTime().before(stockHandle.getHandleDate())) {//事件日期改小了，重算日期采用新的日期
                        recalculateDate = materialHandle.getHandleDate();
                    }
                }
                doctorWarehouseMaterialHandleDao.update(materialHandle);
                warehouseFormulaManager.recalculate(materialHandle, recalculateDate);
            } else {
                doctorWarehouseMaterialHandleDao.update(materialHandle);
            }
        }

        //修改对应的配方入库的单据明细
        if (changeInWarehouse || thisStockHandleChangedQuantity.compareTo(new BigDecimal(0)) != 0 || changeHandleDate) {

            boolean needUpdateInStockHandle = false;//是否需要更新单据
            List<DoctorWarehouseMaterialHandle> inMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandle(inStockHandle.getId());
            if (inMaterialHandles.isEmpty())
                throw new ServiceException("material.handle.not.found");

            DoctorWareHouse oldInWarehouse = new DoctorWareHouse();
            oldInWarehouse.setId(inStockHandle.getWarehouseId());
            oldInWarehouse.setWareHouseName(inStockHandle.getWarehouseName());
            inStockHandle.setSettlementDate(stockDto.getSettlementDate());
            if (!changeInWarehouse) {//如果未改变配方生产入库仓库
                ////如果该单据明细变动了数量，并未改入库仓库，对应的入库单据的明细中的数量也要发生相应变动
                if (thisStockHandleChangedQuantity.compareTo(new BigDecimal(0)) > 0)
                    doctorWarehouseStockManager.in(inMaterialHandles.get(0).getMaterialId(), thisStockHandleChangedQuantity, oldInWarehouse);
                else
                    doctorWarehouseStockManager.out(inMaterialHandles.get(0).getMaterialId(), thisStockHandleChangedQuantity, oldInWarehouse);

                inMaterialHandles.get(0).setQuantity(inMaterialHandles.get(0).getQuantity().add(thisStockHandleChangedQuantity));

                Date recalculateDate = stockHandle.getHandleDate();
                if (changeHandleDate) {
                    warehouseFormulaManager.buildNewHandleDateForUpdate(inMaterialHandles.get(0), stockDto.getHandleDate());
                    int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
                    if (days < 0) {//事件日期改小了，重算日期采用新的日期
                        recalculateDate = inMaterialHandles.get(0).getHandleDate();
                    }
                }
                doctorWarehouseMaterialHandleDao.update(inMaterialHandles.get(0));
                warehouseFormulaManager.recalculate(inMaterialHandles.get(0), recalculateDate);

            } else {//如果改变了配方生产入库仓库
                //原入库仓库扣减库存
                doctorWarehouseStockManager.out(inMaterialHandles.get(0).getMaterialId(), inMaterialHandles.get(0).getQuantity(), oldInWarehouse);
                //原入库仓库删除配方生产入库明细
                warehouseFormulaManager.delete(inMaterialHandles.get(0));

                // 配方出库入库仓库的更改 （陈娟 2018-09-29）
                WarehouseFormulaDto.WarehouseFormulaDetail indetail = new WarehouseFormulaDto.WarehouseFormulaDetail();
                indetail.setMaterialId(inMaterialHandles.get(0).getMaterialId());
                indetail.setWarehouseId(wareHouse.getId());
                indetail.setQuantity(inMaterialHandles.get(0).getQuantity().add(thisStockHandleChangedQuantity));
                // 得到对应的配方入库的单据
                DoctorWarehouseStockHandle stockHandleIn = doctorWarehouseStockHandleDao.findById(stockHandle.getRelStockHandleId());
                // 新的入库仓库添加配方生产入库明细
                DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle = warehouseFormulaManager.create(indetail, stockDto, stockHandleIn, wareHouse);
                // 修改配方出库的关联ID （陈娟 2018-09-29）
                doctorWarehouseMaterialHandleDao.updateRelMaterialHandleId(doctorWarehouseMaterialHandle.getId(), inMaterialHandles.get(0).getId());
                //新的入库仓库添加库存
                doctorWarehouseStockManager.in(inMaterialHandles.get(0).getMaterialId(), indetail.getQuantity(), wareHouse);

                inStockHandle.setWarehouseId(wareHouse.getId());
                inStockHandle.setWarehouseName(wareHouse.getWareHouseName());
                inStockHandle.setWarehouseType(wareHouse.getType());
                needUpdateInStockHandle = true;
            }

            if (changeHandleDate) {
                needUpdateInStockHandle = true;

                buildNewHandleDateForUpdate(inStockHandle, stockDto.getHandleDate());

                Calendar newHandleDate = warehouseFormulaManager.buildNewHandleDate(stockDto.getHandleDate());
                //更新其他出库单据的事件日期
                doctorWarehouseStockHandleDao.updateHandleDateAndSettlementDate(newHandleDate, stockDto.getSettlementDate(), inStockHandle.getId(),1);
                //更新其他出库单据下的明细单据的事件日期
                doctorWarehouseMaterialHandleDao.updateHandleDateAndSettlementDate(newHandleDate, stockDto.getSettlementDate(), inMaterialHandles.get(0).getId(),1);

                // 更新入库单据的事件日期（陈娟 2018-10-08）
                doctorWarehouseStockHandleDao.updateHandleDateAndSettlementDate(newHandleDate, stockDto.getSettlementDate(), inStockHandle.getId(),2);
                doctorWarehouseMaterialHandleDao.updateHandleDateAndSettlementDate(newHandleDate, stockDto.getSettlementDate(), inMaterialHandles.get(0).getId(),2);

            }

            if (needUpdateInStockHandle)
                doctorWarehouseStockHandleDao.update(inStockHandle);

        }
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
