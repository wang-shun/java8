package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWarehouseOrgSettlement;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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
    private JdbcTemplate jdbcTemplate;

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
    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;


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

        List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from v_pc_fiscal_period where company_id=? and deleted_flag=0 and start_date<=? and end_date>=?", new Object[]{1, date, date});
        if (results.isEmpty())
            throw new ServiceException("settlement.date.empty");

        int year = (int) results.get(0).get("fiscal_year");
        int month = (int) results.get(0).get("fiscal_period");

        return new DateTime(year, month, 1, 0, 0).toDate();
    }

    @Override
    @Transactional
    @ExceptionHandle("settlement.fail")
    public Response<Boolean> settlement(Long orgId, List<Long> farmIds, Date settlementDate) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        if (!lock.tryLock())
            throw new ServiceException("under.settlement");

        try {
            //每个仓库下每个物料在该会计年月之前的余额和余量
//            Map<Long/*warehouseId*/, AmountAndQuantityDto> eachWarehouseBalance = doctorWarehouseMaterialHandleDao.findEachWarehouseBalanceBySettlementDate(orgId, settlementDate);
            Map<String/*warehouseId-materialId*/, AmountAndQuantityDto> eachWarehouseBalance = doctorWarehouseStockMonthlyDao.findEachWarehouseBalanceBySettlementDate(orgId, DateUtils.addMonths(settlementDate, -1));

            //已结算的单据明细
            Map<Long/*materialHandleId*/, DoctorWarehouseMaterialHandle> settlementMaterialHandles = new HashMap<>();

            List<DoctorWarehouseMaterialHandle> materialHandles = doctorWarehouseMaterialHandleDao.findByOrgAndSettlementDate(orgId, settlementDate);
            for (DoctorWarehouseMaterialHandle materialHandle : materialHandles) {

                AmountAndQuantityDto newHistoryBalance = CalcUnitPrice(materialHandle,
                        eachWarehouseBalance.getOrDefault(materialHandle.getWarehouseId() + "-" + materialHandle.getMaterialId(), new AmountAndQuantityDto()),
                        settlementMaterialHandles);

                //更新余额和余量
                eachWarehouseBalance.put(materialHandle.getWarehouseId() + "-" + materialHandle.getMaterialId(), newHistoryBalance);

                settlementMaterialHandles.put(materialHandle.getId(), materialHandle);
            }

            //统计各个仓库下各个物料余额和余量
            farmIds.stream().forEach(f -> {
                doctorWareHouseDao.findByFarmId(f).forEach(wareHouse -> {
                    doctorWarehouseStockDao.findSkuIds(wareHouse.getId()).forEach(sku -> {

                        //上一个会计年月的余额和余量
                        DoctorWarehouseStockMonthly balance = doctorWarehouseStockMonthlyDao.findBalanceBySettlementDate(wareHouse.getId(), sku, DateUtils.addMonths(settlementDate, -1));
                        BigDecimal balanceAmount, balanceQuantity;
                        if (null == balance || null == balance.getBalanceAmount())
                            balanceAmount = new BigDecimal(0);
                        else balanceAmount = balance.getBalanceAmount();
                        if (null == balance || null == balance.getBalanceQuantity())
                            balanceQuantity = new BigDecimal(0);
                        else balanceQuantity = balance.getBalanceQuantity();

                        //本会计年月发生额和发生量
                        AmountAndQuantityDto thisSettlementAmountAndQuantity = doctorWarehouseMaterialHandleDao.findBalanceBySettlementDate(wareHouse.getId(), sku, settlementDate);

                        //创建或更新月度统计
                        DoctorWarehouseStockMonthly stockMonthly = doctorWarehouseStockMonthlyDao.findBalanceBySettlementDate(wareHouse.getId(), sku, settlementDate);
                        if (null == stockMonthly)
                            stockMonthly = new DoctorWarehouseStockMonthly();
                        stockMonthly.setOrgId(orgId);
                        stockMonthly.setFarmId(f);
                        stockMonthly.setWarehouseId(wareHouse.getId());
                        stockMonthly.setMaterialId(sku);
                        stockMonthly.setSettlementDate(settlementDate);
                        stockMonthly.setBalanceAmount(balanceAmount.add(thisSettlementAmountAndQuantity.getAmount()));
                        stockMonthly.setBalanceQuantity(balanceQuantity.add(thisSettlementAmountAndQuantity.getQuantity()));
                        if (null == stockMonthly.getId())
                            doctorWarehouseStockMonthlyDao.create(stockMonthly);
                        else doctorWarehouseStockMonthlyDao.update(stockMonthly);
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
                } else {
                    log.info("previous in not found,use user set unit price :{}", materialHandle.getUnitPrice());
                }
            } else if (materialHandle.getType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue())) {
                //配方生产入库，根据出库的总价/入库的数量
                //获取配方入库单据
                DoctorWarehouseStockHandle formulaInStockHandle = doctorWarehouseStockHandleDao.findById(materialHandle.getStockHandleId());
                if (null == formulaInStockHandle)
                    throw new InvalidException("stock.handle.not.found", materialHandle.getStockHandleId());
                //根据配方入库单据获取对应配方出库单据，再根据配方出库单据获取明细
                List<Long> formulaOutStockHandleIds = doctorWarehouseStockHandleDao.findRelStockHandle(formulaInStockHandle.getId()).stream().map(DoctorWarehouseStockHandle::getId).collect(Collectors.toList());
                if (formulaOutStockHandleIds.isEmpty())
                    throw new InvalidException("formula.out.stock.handle.not.found", formulaInStockHandle.getId());

                List<DoctorWarehouseMaterialHandle> formulaOutMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandles(formulaOutStockHandleIds);
                if (formulaOutMaterialHandles.isEmpty())
                    throw new InvalidException("formula.out.material.handle.not.found", formulaInStockHandle.getId());

                BigDecimal totalFormulaOutAmount = formulaOutMaterialHandles
                        .stream()
                        .map(mh -> new BigDecimal(mh.getUnitPrice().toString()).multiply(mh.getQuantity()))
                        .reduce((a, b) -> a.add(b))
                        .orElse(new BigDecimal(0));

                materialHandle.setUnitPrice(totalFormulaOutAmount.divide(materialHandle.getQuantity(), 4, BigDecimal.ROUND_HALF_UP));

            } else if (materialHandle.getType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue())
                    || materialHandle.getType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {

                DoctorWarehouseMaterialHandle otherIn = settlementMaterialHandles.get(materialHandle.getRelMaterialHandleId());
                if (null == otherIn) {
                    if (materialHandle.getType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {
                        //可能对于的出库单据明细已经结算
                        otherIn = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());
                        if (null == otherIn || otherIn.getDeleteFlag().equals(WarehouseMaterialHandleDeleteFlag.DELETE.getValue()))
                            throw new InvalidException("material.handle.not.found", materialHandle.getRelMaterialHandleId());
                    } else
                        throw new InvalidException("material.handle.not.found", materialHandle.getRelMaterialHandleId());
                }

                materialHandle.setUnitPrice(otherIn.getUnitPrice());
            }

            historyStockQuantity = historyStockQuantity.add(materialHandle.getQuantity());
            historyStockAmount = historyStockAmount.add(new BigDecimal(materialHandle.getUnitPrice().toString()).multiply(materialHandle.getQuantity()));
        } else {
            //出库类型：领料出库，盘亏出库，调拨出库，配方生产出库
            if (historyStockAmount.equals(new BigDecimal(0)) || historyStockQuantity.equals(new BigDecimal(0))) {
                log.error("history amount or quantity is zero,can not settlement for material handle:{}", materialHandle.getId());
            }
            materialHandle.setUnitPrice(new BigDecimal(historyStockAmount.toString()).divide(historyStockQuantity, 4, BigDecimal.ROUND_HALF_UP));

            if (materialHandle.getType().equals(WarehouseMaterialHandleType.OUT.getValue())) {
                doctorWarehouseMaterialApplyDao.updateUnitPriceAndAmountByMaterialHandle(materialHandle.getId(), materialHandle.getUnitPrice(), materialHandle.getQuantity().multiply(materialHandle.getUnitPrice()));
            }

            historyStockQuantity = historyStockQuantity.subtract(materialHandle.getQuantity());
            historyStockAmount = historyStockAmount.subtract(new BigDecimal(materialHandle.getUnitPrice().toString()).multiply(materialHandle.getQuantity()));
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

        //清空明细单的单价和金额
        doctorWarehouseMaterialHandleDao.reverseSettlement(orgId, settlementDate);
        //清空领用的单价和金额
        doctorWarehouseMaterialApplyDao.reverseSettlement(orgId, settlementDate);
        //删除月度结余
        doctorWarehouseStockMonthlyDao.reverseSettlement(orgId, settlementDate);

        settlement.setLastSettlementDate(DateUtils.addMonths(settlement.getLastSettlementDate(), -1));
        doctorWarehouseOrgSettlementDao.update(settlement);
        return Response.ok();
    }
}
