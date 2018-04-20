package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWarehouseOrgSettlement;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;
import io.terminus.doctor.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * 结算服务
 * Created by sunbo@terminus.io on 2018/4/11.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseSettlementServiceImpl implements DoctorWarehouseSettlementService {


    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;

    @Autowired
    private DoctorWarehouseOrgSettlementDao doctorWarehouseOrgSettlementDao;

    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;


    @Override
    public boolean isUnderSettlement(Long orgId) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        boolean locked = lock.tryLock();
        if (locked)
            lock.unlock();

        return !locked;
    }

    @Override
    public boolean isSettled(Long orgId, Date settlementDate) {
        return doctorWarehouseOrgSettlementDao.isSettled(orgId, settlementDate);
    }

    @Override
    public Date getSettlementDate(Date date) {

        Date settlementDate = new Date();
        if (null == settlementDate)
            throw new ServiceException("get.settlement.date.fail");
        return settlementDate;
    }

    @Override
    @Transactional
    @ExceptionHandle("settlement.fail")
    public Response<Boolean> settlement(Long orgId, List<Long> farmIds, Date settlementDate) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        if (!lock.tryLock())
            throw new ServiceException("under.settlement");

        try {
            //每个仓库在该会计年月之前的余额和余量
//            Map<Long/*warehouseId*/, AmountAndQuantityDto> eachWarehouseBalance = doctorWarehouseMaterialHandleDao.findEachWarehouseBalanceBySettlementDate(orgId, settlementDate);
            Map<Long/*warehouseId*/, AmountAndQuantityDto> eachWarehouseBalance = doctorWarehouseStockMonthlyDao.findEachWarehouseBalanceBySettlementDate(orgId, DateUtils.addMonths(settlementDate, -1));

            //已结算的单据明细
            Map<Long/*materialHandleId*/, DoctorWarehouseMaterialHandle> settlementMaterialHandles = new HashMap<>();

            List<DoctorWarehouseMaterialHandle> materialHandles = doctorWarehouseMaterialHandleDao.findByOrgAndSettlementDate(orgId, settlementDate);
            for (DoctorWarehouseMaterialHandle materialHandle : materialHandles) {

                AmountAndQuantityDto newHistoryBalance = CalcUnitPrice(materialHandle,
                        eachWarehouseBalance.getOrDefault(materialHandle.getWarehouseId(), new AmountAndQuantityDto()),
                        settlementMaterialHandles);

                //更新余额和余量
                eachWarehouseBalance.put(materialHandle.getWarehouseId(), newHistoryBalance);

                settlementMaterialHandles.put(materialHandle.getId(), materialHandle);
            }

            //统计各个仓库下各个物料在该会计年月内的发生额和发生量
            farmIds.stream().forEach(f -> {
                doctorWareHouseDao.findByFarmId(f).forEach(wareHouse -> {
                    doctorWarehouseStockDao.findSkuIds(wareHouse.getId()).forEach(sku -> {

                        //上一个会计年月的余额和余量
                        AmountAndQuantityDto balance = doctorWarehouseStockMonthlyDao.findBalanceBySettlementDate(wareHouse.getId(), sku, DateUtils.addMonths(settlementDate, -1));

                        BigDecimal totalAmount = new BigDecimal(0);
                        BigDecimal totalQuantity = new BigDecimal(0);
                        settlementMaterialHandles.values()
                                .stream()
                                .filter(m -> m.getWarehouseId().equals(wareHouse.getId()) && m.getMaterialId().equals(sku))
                                .forEach(m -> {
                                    totalAmount.add(m.getAmount());
                                    totalQuantity.add(m.getQuantity());
                                });

                        DoctorWarehouseStockMonthly stockMonthly = new DoctorWarehouseStockMonthly();
                        stockMonthly.setOrgId(orgId);
                        stockMonthly.setFarmId(f);
                        stockMonthly.setMaterialId(sku);
                        stockMonthly.setSettlementDate(settlementDate);
                        stockMonthly.setBalanceAmount(balance.getAmount().add(totalAmount));
                        stockMonthly.setBalanceQuantity(balance.getQuantity().add(totalQuantity));
                        doctorWarehouseStockMonthlyDao.create(stockMonthly);
                    });
                });
            });

            DoctorWarehouseOrgSettlement settlement = doctorWarehouseOrgSettlementDao.findByOrg(orgId);
            if (null == settlement) {
                settlement = new DoctorWarehouseOrgSettlement();
                settlement.setOrgId(orgId);
                settlement.setLastSettlementDate(settlementDate);
                doctorWarehouseOrgSettlementDao.create(settlement);
            } else {
                settlement.setLastSettlementDate(settlementDate);
                doctorWarehouseOrgSettlementDao.update(settlement);
            }

            return Response.ok();

        } finally {
            lock.unlock();
        }
    }


    /**
     * 结算某一个仓库下某个会计区间的单据明细
     */
    @Deprecated
    private void settlement(Long warehouseId, DateTime settlementDate, List<Long> settledWarehouses) {

        if (settledWarehouses.contains(warehouseId))
            return;

        log.info("start to settlement warehouse {} at {}-{}", warehouseId, settlementDate.getYear(), settlementDate.getMonthOfYear());

        //获取本仓库该会计年月之前的总库存量和总金额
        AmountAndQuantityDto amountAndQuantityDto = doctorWarehouseMaterialHandleDao.findBalanceByAccountingDate(warehouseId, settlementDate.toDate());

        BigDecimal historyStockQuantity = amountAndQuantityDto.getQuantity();
        BigDecimal historyStockAmount = amountAndQuantityDto.getAmount();

        log.debug("before {}-{} stock quantity is {} and amount is {}", settlementDate.getYear(), settlementDate.getMonthOfYear(), historyStockQuantity, historyStockAmount);

        //获取本仓库该会计年月，需要结算的单据明细
        List<DoctorWarehouseMaterialHandle> materialHandles = doctorWarehouseMaterialHandleDao.findByAccountingDate(warehouseId, settlementDate.getYear(), settlementDate.getMonthOfYear());

        if (materialHandles.isEmpty())
            log.warn("no material handle to be settlement at {}", settlementDate);

        for (DoctorWarehouseMaterialHandle m : materialHandles) {

            log.debug("settlement for material handle {},material {},warehouse {},quantity {}", m.getId(), m.getMaterialName(), m.getWarehouseName(), m.getQuantity());

            if (WarehouseMaterialHandleType.isBigIn(m.getType())) {
                //入库类型：采购入库，退料入库，盘盈入库，调拨入库，配方生产入库

                //盘盈单的单价采用上一笔采购入库单的单价
                if (m.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())) {
                    //获取上一笔采购入库单
                    DoctorWarehouseMaterialHandle previousIn = doctorWarehouseMaterialHandleDao.findPrevious(m, WarehouseMaterialHandleType.IN);
                    if (null != previousIn) {
                        log.debug("use previous material handle[purchase in] unit price :{}", previousIn.getUnitPrice());
                        m.setUnitPrice(previousIn.getUnitPrice());
                    }
                } else if (m.getType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue())) {
                    //配方生产入库，根据出库的总价/入库的数量
                    DoctorWarehouseStockHandle formulaInStockHandle = doctorWarehouseStockHandleDao.findById(m.getStockHandleId());
                    if (null == formulaInStockHandle)
                        throw new ServiceException("stock.handle.not.found");
                    DoctorWarehouseStockHandle formulaOutStockHandle = doctorWarehouseStockHandleDao.findById(formulaInStockHandle.getRelStockHandleId());
                    if (null == formulaOutStockHandle)
                        throw new ServiceException("stock.handle.not.found");
                    List<DoctorWarehouseMaterialHandle> formulaOutMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandle(formulaOutStockHandle.getId());

                    BigDecimal totalFormulaOutAmount = formulaOutMaterialHandles
                            .stream()
                            .map(mh -> new BigDecimal(mh.getUnitPrice().toString()).multiply(mh.getQuantity()))
                            .reduce((a, b) -> a.add(b))
                            .orElse(new BigDecimal(0));

                    m.setUnitPrice(totalFormulaOutAmount.divide(m.getQuantity(), 4, BigDecimal.ROUND_HALF_UP));

                } else if (m.getType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue())

                        || m.getType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {

                    DoctorWarehouseMaterialHandle otherIn = doctorWarehouseMaterialHandleDao.findById(m.getRelMaterialHandleId());
                    if (null == otherIn)
                        throw new ServiceException("material.handle.not.found");
                    if (null != otherIn.getUnitPrice())
                        m.setUnitPrice(otherIn.getUnitPrice());
                    else {
                        settlement(otherIn.getWarehouseId(), settlementDate, settledWarehouses);
                        m.setUnitPrice(doctorWarehouseMaterialHandleDao.findById(m.getRelMaterialHandleId()).getUnitPrice());
                    }
                }

                historyStockQuantity = historyStockQuantity.add(m.getQuantity());
                historyStockAmount = historyStockAmount.add(new BigDecimal(m.getUnitPrice().toString()).multiply(m.getQuantity()));
            } else {
                //出库类型：领料出库，盘亏出库，调拨出库，配方生产出库

                m.setUnitPrice(new BigDecimal(historyStockAmount.toString()).divide(historyStockQuantity, 4, BigDecimal.ROUND_HALF_UP));

                historyStockQuantity = historyStockQuantity.subtract(m.getQuantity());
                historyStockAmount = historyStockAmount.add(new BigDecimal(m.getUnitPrice().toString()).multiply(m.getQuantity()));
            }

            doctorWarehouseMaterialHandleDao.update(m);
        }

        settledWarehouses.add(warehouseId);
    }


    /**
     * 计算单价
     *
     * @param materialHandle            需要计算的单据明细
     * @param historyBalance            历史余额和余量
     * @param settlementMaterialHandles 已被计算好的单据明细
     * @return 新的历史余额和余量
     */
    private AmountAndQuantityDto CalcUnitPrice(DoctorWarehouseMaterialHandle materialHandle,
                                               AmountAndQuantityDto historyBalance,
                                               Map<Long, DoctorWarehouseMaterialHandle> settlementMaterialHandles) {
        log.debug("settlement for material handle {},material {},warehouse {},quantity {}",
                materialHandle.getId(),
                materialHandle.getMaterialName(),
                materialHandle.getWarehouseName(),
                materialHandle.getQuantity());

        BigDecimal historyStockAmount = historyBalance.getAmount();
        BigDecimal historyStockQuantity = historyBalance.getQuantity();

        if (WarehouseMaterialHandleType.isBigIn(materialHandle.getType())) {
            //入库类型：采购入库，退料入库，盘盈入库，调拨入库，配方生产入库


            if (materialHandle.getType().equals(WarehouseMaterialHandleType.IN.getValue())) {
                return new AmountAndQuantityDto(historyStockAmount.add(materialHandle.getAmount()), historyStockQuantity.add(materialHandle.getQuantity()));
            }

            //盘盈单的单价采用上一笔采购入库单的单价
            if (materialHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())) {
                //获取上一笔采购入库单
                DoctorWarehouseMaterialHandle previousIn = doctorWarehouseMaterialHandleDao.findPrevious(materialHandle, WarehouseMaterialHandleType.IN);
                if (null != previousIn) {
                    log.debug("use previous material handle[purchase in] unit price :{}", previousIn.getUnitPrice());
                    materialHandle.setUnitPrice(previousIn.getUnitPrice());
                }
            } else if (materialHandle.getType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue())) {
                //配方生产入库，根据出库的总价/入库的数量
                DoctorWarehouseStockHandle formulaInStockHandle = doctorWarehouseStockHandleDao.findById(materialHandle.getStockHandleId());
                if (null == formulaInStockHandle)
                    throw new ServiceException("stock.handle.not.found");
                List<DoctorWarehouseMaterialHandle> formulaOutMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandle(formulaInStockHandle.getRelStockHandleId());

                BigDecimal totalFormulaOutAmount = formulaOutMaterialHandles
                        .stream()
                        .map(mh -> new BigDecimal(mh.getUnitPrice().toString()).multiply(mh.getQuantity()))
                        .reduce((a, b) -> a.add(b))
                        .orElse(new BigDecimal(0));

                materialHandle.setUnitPrice(totalFormulaOutAmount.divide(materialHandle.getQuantity(), 4, BigDecimal.ROUND_HALF_UP));

            } else if (materialHandle.getType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue())
                    || materialHandle.getType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {

                DoctorWarehouseMaterialHandle otherIn = settlementMaterialHandles.get(materialHandle.getRelMaterialHandleId());
                if (null == otherIn)
                    throw new ServiceException("material.handle.not.found");

                materialHandle.setUnitPrice(otherIn.getUnitPrice());
            }

            historyStockQuantity = historyStockQuantity.add(materialHandle.getQuantity());
            historyStockAmount = historyStockAmount.add(new BigDecimal(materialHandle.getUnitPrice().toString()).multiply(materialHandle.getQuantity()));
        } else {
            //出库类型：领料出库，盘亏出库，调拨出库，配方生产出库
            materialHandle.setUnitPrice(new BigDecimal(historyStockAmount.toString()).divide(historyStockQuantity, 4, BigDecimal.ROUND_HALF_UP));

            historyStockQuantity = historyStockQuantity.subtract(materialHandle.getQuantity());
            historyStockAmount = historyStockAmount.add(new BigDecimal(materialHandle.getUnitPrice().toString()).multiply(materialHandle.getQuantity()));
        }

        materialHandle.setAmount(materialHandle.getUnitPrice().multiply(materialHandle.getQuantity()));
        doctorWarehouseMaterialHandleDao.update(materialHandle);
        return new AmountAndQuantityDto(historyStockAmount, historyStockQuantity);
    }


    @Override
    @Transactional
    @ExceptionHandle("anti.settlement.fail")
    public Response<Boolean> antiSettlement(Long orgId, List<Long> farmIds, Date settlementDate) {

        DateTime date = new DateTime(settlementDate);

        DoctorWarehouseOrgSettlement settlement = doctorWarehouseOrgSettlementDao.findByOrg(orgId);
        if (null == settlement) {
            log.warn("org settlement is missing");
            return Response.ok();
        }

        if (settlementDate.after(settlement.getLastSettlementDate())) {
            throw new ServiceException("anti.settlement.future");
        }
        if (settlementDate.before(settlement.getLastSettlementDate())) {
            throw new ServiceException("anti.settlement.first");
        }

        //清空明细单的单价
        doctorWarehouseMaterialHandleDao.reverseSettlement(orgId, settlementDate);
        //删除月度结余
        doctorWarehouseStockMonthlyDao.reverseSettlement(orgId, settlementDate);

        settlement.setLastSettlementDate(DateUtils.addMonths(settlement.getLastSettlementDate(), -1));
        doctorWarehouseOrgSettlementDao.update(settlement);
        return Response.ok();
    }
}
