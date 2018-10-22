package io.terminus.doctor.basic.service.warehouseV2;

import ch.qos.logback.core.util.TimeUtil;
import com.google.common.base.Stopwatch;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Boolean findByOrgId(Long orgId) {
        DoctorWarehouseOrgSettlement list = doctorWarehouseOrgSettlementDao.findByOrgId(orgId);
        if(list!=null){
            return true;
        }else{
            return false;
        }
    }

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
        date = DateUtil.toDate(DateUtil.toDateString(date));
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
            DoctorWarehouseOrgSettlement settlement = doctorWarehouseOrgSettlementDao.findByOrg(orgId);
            if (null != settlement) {
                log.error("settlement:"+DateUtils.addMonths(settlement.getLastSettlementDate(), 1).toGMTString());
                log.error("settlementDate:"+settlementDate.toGMTString());
                if (DateUtil.toDate(DateUtil.getYearMonth(settlementDate) + "-01").after(DateUtils.addMonths(settlement.getLastSettlementDate(), 1))) {
                    throw new ServiceException("settlement.future");
                }
            }

            //每个仓库下每个物料在该会计年月之前的余额和余量
//            Map<Long/*warehouseId*/, AmountAndQuantityDto> eachWarehouseBalance = doctorWarehouseMaterialHandleDao.findEachWarehouseBalanceBySettlementDate(orgId, settlementDate);
            Map<String/*warehouseId-materialId*/, AmountAndQuantityDto> eachWarehouseBalance = doctorWarehouseStockMonthlyDao.findWarehouseBalanceBySettlementDate(orgId, DateUtils.addMonths(settlementDate, -1));

            //已结算的单据明细
            Map<Long/*materialHandleId*/, DoctorWarehouseMaterialHandle> settlementMaterialHandles = new HashMap<>();
            Stopwatch stopwatch = Stopwatch.createStarted();


            log.info("start to settlement org {} material handle for {}", settlementDate);

//            // 配方入库金额，单价的结算（陈娟 2018-09-28）
//            // 得到配方入库单据
//            List<DoctorWarehouseMaterialHandle> materialHandleIns = doctorWarehouseMaterialHandleDao.findFormulaStorage(orgId, settlementDate);
//            for (DoctorWarehouseMaterialHandle materialHandleIn : materialHandleIns) {
//                List<DoctorWarehouseMaterialHandle> materialHandleOuts = doctorWarehouseMaterialHandleDao.findFormulaByRelMaterialHandleId(materialHandleIn.getId(), 12);
//                // 得到配方出库单据
//                BigDecimal amout = new BigDecimal(0);
//                for (DoctorWarehouseMaterialHandle materialHandleOut : materialHandleOuts) {
//                    amout = amout.add(materialHandleOut.getAmount());
//                }
//                doctorWarehouseMaterialHandleDao.updateUnitPriceAndAmountById(materialHandleIn.getId(), amout.divide(materialHandleIn.getQuantity()), amout);
//            }
//
//            log.info("update formula storage unit price and amount under org {} use :{}ms", orgId, stopwatch.elapsed(TimeUnit.MILLISECONDS));

            // 计算单价、金额时不需要得到配方入库单据，配方入库的单价、金额计算配方出库的时候累加计算 （陈娟 2018-10-09）
            List<DoctorWarehouseMaterialHandle> materialHandles = doctorWarehouseMaterialHandleDao.findByOrgAndSettlementDate(orgId, settlementDate,1);

            log.info("get all need to settlement material handle,total :{}", materialHandles.size());

            for (DoctorWarehouseMaterialHandle materialHandle : materialHandles) {

                AmountAndQuantityDto lastSettlementBalance = eachWarehouseBalance.get(materialHandle.getWarehouseId() + "-" + materialHandle.getMaterialId());
                if (null == lastSettlementBalance) {
                    log.warn("no balance found for warehouse:{},material:{},init amount to 0,quantity to 0", materialHandle.getWarehouseId(), materialHandle.getMaterialId());
                    lastSettlementBalance = new AmountAndQuantityDto();
                } else {
                    log.debug("start calc unit price for material {},history amount {},history quantity {}", materialHandle.getId(), lastSettlementBalance.getAmount(), lastSettlementBalance.getQuantity());
                }

                AmountAndQuantityDto newHistoryBalance = CalcUnitPrice(materialHandle,
                        lastSettlementBalance,
                        settlementMaterialHandles);

                //更新余额和余量
                eachWarehouseBalance.put(materialHandle.getWarehouseId() + "-" + materialHandle.getMaterialId(), newHistoryBalance);

                settlementMaterialHandles.put(materialHandle.getId(), materialHandle);
            }

            log.info("calc material handle unit price and amount under org {} use :{}ms ", orgId, stopwatch.elapsed(TimeUnit.MILLISECONDS));

            doctorWarehouseMaterialHandleDao.updates(materialHandles);

            log.info("update material handle unit price and amount under org {} use :{}ms", orgId, stopwatch.elapsed(TimeUnit.MILLISECONDS));

//            //处理结算金额的误差（陈娟 2018-8-21）
//            for (DoctorWarehouseMaterialHandle materialHandle : materialHandles) {
//
//                //得到上月结存金额和数量
//                Map<String, Object> lastMap = doctorWarehouseMaterialHandleDao.getLastAmount(materialHandle.getWarehouseId(), materialHandle.getMaterialId(), materialHandle.getSettlementDate());
//                //得到本月结存金额和数量
//                Map<String, Object> thisMap = doctorWarehouseMaterialHandleDao.getThisAmount(materialHandle.getWarehouseId(), materialHandle.getMaterialId(), materialHandle.getSettlementDate());
//                BigDecimal thisQuantity =(BigDecimal) thisMap.get("thisQuantity");
//                BigDecimal thisAmount =(BigDecimal) thisMap.get("thisAmount");
//
//                //得到最后一笔单据，把误差算进去
//                DoctorWarehouseMaterialHandle lastDocument = doctorWarehouseMaterialHandleDao.getLastDocument(materialHandle.getWarehouseId(), materialHandle.getMaterialId(), materialHandle.getSettlementDate());
//                if(lastMap!=null){
//                    BigDecimal lastQuantity =(BigDecimal) lastMap.get("lastQuantity");
//                    BigDecimal lastAmount =(BigDecimal) lastMap.get("lastAmount");
//                    //上月结存数量减去本月结存数量等于0  并且上月结存金额减去本月结存金额不等于0
//                    if((lastQuantity.add(thisQuantity).compareTo(BigDecimal.ZERO)==0)&&(lastAmount.add(thisAmount).compareTo(BigDecimal.ZERO)!=0)){
//                        //得到最后的金额
//                        BigDecimal finalAmount1 = lastDocument.getAmount().add(lastAmount.add(thisAmount));
//                        log.info("finalAmount11111:"+finalAmount1);
//                        lastDocument.setAmount(finalAmount1.setScale(2,BigDecimal.ROUND_HALF_UP));
//                        lastDocument.setUnitPrice(finalAmount1.divide(lastDocument.getQuantity(),4,BigDecimal.ROUND_HALF_UP));
//                        //修改单据
//                        doctorWarehouseMaterialHandleDao.update(lastDocument);
//                    }
//                }else{
//                    //本月结存数量等于0  并且本月结存金额不等于0
//                    if((thisQuantity.compareTo(BigDecimal.ZERO)==0)&&(thisAmount.compareTo(BigDecimal.ZERO)!=0)){
//                        //得到最后的金额
//                        BigDecimal finalAmount2 = lastDocument.getAmount().add(thisAmount);
//                        log.info("finalAmount22222:"+finalAmount2);
//                        lastDocument.setAmount(finalAmount2.setScale(2,BigDecimal.ROUND_HALF_UP));
//                        lastDocument.setUnitPrice(finalAmount2.divide(lastDocument.getQuantity(),4,BigDecimal.ROUND_HALF_UP));
//                        //修改单据
//                        doctorWarehouseMaterialHandleDao.update(lastDocument);
//                    }
//                }
//
//            }
//            log.info("update error amount under org {} use :{}ms", orgId, stopwatch.elapsed(TimeUnit.MILLISECONDS));

            // 结算表需要得到配方入库单据 （陈娟 2018-10-09）
            List<DoctorWarehouseMaterialHandle> materialHandles2 = doctorWarehouseMaterialHandleDao.findByOrgAndSettlementDate(orgId, settlementDate,0);
            //结算本月有出入库的物料
            Map<Long/*farmId*/, List<DoctorWarehouseMaterialHandle>> warehouseMaterialHandleMap = materialHandles2.
                    stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getWarehouseId));

            warehouseMaterialHandleMap.forEach((warehouseId, warehouseMaterialHandles) -> {
                warehouseMaterialHandles.stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId)).forEach((sku, skuMaterialHandles) -> {
                    log.error("start record warehouse {},sku {}", warehouseId, sku);

                    //上一个会计年月的余额和余量
                    DoctorWarehouseStockMonthly balance = doctorWarehouseStockMonthlyDao.findBalanceBySettlementDate(warehouseId, sku, DateUtils.addMonths(settlementDate, -1));
                    BigDecimal balanceAmount, balanceQuantity;
                    if (null == balance || null == balance.getBalanceAmount())
                        balanceAmount = new BigDecimal(0);
                    else balanceAmount = balance.getBalanceAmount();
                    if (null == balance || null == balance.getBalanceQuantity())
                        balanceQuantity = new BigDecimal(0);
                    else balanceQuantity = balance.getBalanceQuantity();

                    //本会计年月发生额和发生量
                    AmountAndQuantityDto thisSettlementAmountAndQuantity = doctorWarehouseMaterialHandleDao.findBalanceBySettlementDate(warehouseId, sku, settlementDate);

                    //创建或更新月度统计
                    DoctorWarehouseStockMonthly stockMonthly = doctorWarehouseStockMonthlyDao.findBalanceBySettlementDate(warehouseId, sku, settlementDate);
                    if (null == stockMonthly)
                        stockMonthly = new DoctorWarehouseStockMonthly();
                    stockMonthly.setOrgId(orgId);
                    stockMonthly.setFarmId(skuMaterialHandles.get(0).getFarmId());
                    stockMonthly.setWarehouseId(warehouseId);
                    stockMonthly.setMaterialId(sku);
                    stockMonthly.setSettlementDate(settlementDate);
                    stockMonthly.setBalanceAmount(balanceAmount.add(thisSettlementAmountAndQuantity.getAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
                    stockMonthly.setBalanceQuantity(balanceQuantity.add(thisSettlementAmountAndQuantity.getQuantity()).setScale(3,BigDecimal.ROUND_HALF_UP));
                    if (null == stockMonthly.getId())
                        doctorWarehouseStockMonthlyDao.create(stockMonthly);
                    else doctorWarehouseStockMonthlyDao.update(stockMonthly);
                });

            });

            // 结算本月无出入库，但上月有出入库的物料
            List<Map> warehouseIdByOrgId = doctorWarehouseStockMonthlyDao.findWarehouseIdByOrgId(orgId, DateUtils.addMonths(settlementDate, -1));
            for (Map ww:warehouseIdByOrgId) {
                Long warehouseId = (Long) ww.get("warehouse_id");
                List<DoctorWarehouseStockMonthly> maps = doctorWarehouseStockMonthlyDao.copyDoctorWarehouseWtockMonthly(warehouseId, DateUtils.addMonths(settlementDate, -1), settlementDate);
                if(maps.size()>0) {
                    for (DoctorWarehouseStockMonthly sm : maps) {
                        DoctorWarehouseStockMonthly stockMonthly = new DoctorWarehouseStockMonthly();
                        stockMonthly.setOrgId(sm.getOrgId());
                        stockMonthly.setFarmId(sm.getFarmId());
                        stockMonthly.setWarehouseId(sm.getWarehouseId());
                        stockMonthly.setMaterialId(sm.getMaterialId());
                        stockMonthly.setBalanceQuantity(sm.getBalanceQuantity().setScale(3,BigDecimal.ROUND_HALF_UP));
                        stockMonthly.setBalanceAmount(sm.getBalanceAmount().setScale(2,BigDecimal.ROUND_HALF_UP));
                        stockMonthly.setSettlementDate(DateUtils.addMonths(sm.getSettlementDate(), 1));
                        doctorWarehouseStockMonthlyDao.create(stockMonthly);
                    }
                }
            }

            log.info("update or create stock monthly balance under org {} use :{}ms", orgId, stopwatch.elapsed(TimeUnit.MILLISECONDS));

            if (null == settlement) {
                settlement = new DoctorWarehouseOrgSettlement();
                settlement.setOrgId(orgId);
                settlement.setLastSettlementDate(settlementDate);
                doctorWarehouseOrgSettlementDao.create(settlement);
            } else {
                settlement.setLastSettlementDate(settlementDate);
                doctorWarehouseOrgSettlementDao.update(settlement);
            }
            log.info("settlement:{}",settlement);
            return Response.ok();

        } catch (InvalidException e) {
            log.error("message:{},params:{}", e.getError(), Stream.of(e.getParams()).map(o -> o.toString()).collect(Collectors.joining(",")));
            throw e;
        } catch (Exception e) {
            log.error("settlement fail,", e);
            throw e;
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
                materialHandle.getMaterialId(),
                materialHandle.getWarehouseId(),
                materialHandle.getQuantity());

        BigDecimal historyStockAmount = new BigDecimal(0);
        BigDecimal historyStockQuantity = new BigDecimal(0);
        // 如果该物料在出库之前只有配方入库，则此时的出库单据的单价与金额和配方出库息息相关（陈娟 2018-10-08）
        if(materialHandle.getType().equals(WarehouseMaterialHandleType.OUT.getValue())){
            List<Map> typeMap = doctorWarehouseMaterialHandleDao.getBeforeType(materialHandle.getWarehouseId(), materialHandle.getSettlementDate(), materialHandle.getMaterialId(),materialHandle.getId());
            if((typeMap.size()==1&&typeMap.get(0).get("type").equals(WarehouseMaterialHandleType.FORMULA_IN.getValue()))||typeMap.size()>1){
                // 只有配方入库或者有配方入库也有采购入库时，历史金额(数量)等于配方 + 此时的历史金额(数量)
                Map<String, Object> recipesSum = doctorWarehouseMaterialHandleDao.getBeforeRecipes(materialHandle.getWarehouseId(), materialHandle.getSettlementDate(), materialHandle.getMaterialId(),materialHandle.getId());
                historyStockAmount = historyBalance.getAmount().add(new BigDecimal(recipesSum.get("sumAmount").toString()));
                historyStockQuantity = historyBalance.getQuantity().add(new BigDecimal(recipesSum.get("sumQuantity").toString()));
            }else{
                historyStockAmount = historyBalance.getAmount();
                historyStockQuantity = historyBalance.getQuantity();
            }
        }else{
            historyStockAmount = historyBalance.getAmount();
            historyStockQuantity = historyBalance.getQuantity();
        }

        if (WarehouseMaterialHandleType.isBigIn(materialHandle.getType())) {
            //入库类型：采购入库，退料入库，盘盈入库，调拨入库，配方生产入库

            if (materialHandle.getType().equals(WarehouseMaterialHandleType.IN.getValue())) {
                return new AmountAndQuantityDto(historyStockAmount.add(materialHandle.getAmount().setScale(2,BigDecimal.ROUND_HALF_UP)), historyStockQuantity.add(materialHandle.getQuantity().setScale(3,BigDecimal.ROUND_HALF_UP)));
            }

            //盘盈单的单价采用上一笔采购入库单的单价
            if (materialHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())) {
                //获取上一笔采购入库单
                DoctorWarehouseMaterialHandle previousIn = doctorWarehouseMaterialHandleDao.findPrevious(materialHandle, WarehouseMaterialHandleType.IN);
                if (null != previousIn) {
                    log.debug("use previous material handle[purchase in] unit price :{}", previousIn.getUnitPrice());
                    materialHandle.setUnitPrice(previousIn.getUnitPrice().setScale(4,BigDecimal.ROUND_HALF_UP));
                } else {
                    log.info("previous in not found,use user set unit price :{}", materialHandle.getUnitPrice());
                }
                materialHandle.setAmount(materialHandle.getUnitPrice().multiply(materialHandle.getQuantity()).setScale(2,BigDecimal.ROUND_HALF_UP));
//            } else if (materialHandle.getType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue())) {
//                //配方生产入库，根据出库的总价/入库的数量
//                //获取配方入库单据
//                DoctorWarehouseStockHandle formulaInStockHandle = doctorWarehouseStockHandleDao.findById(materialHandle.getStockHandleId());
//                if (null == formulaInStockHandle)
//                    throw new InvalidException("stock.handle.not.found", materialHandle.getStockHandleId());
//                //根据配方入库单据获取对应配方出库单据，再根据配方出库单据获取明细
//                List<Long> formulaOutStockHandleIds = doctorWarehouseStockHandleDao.findRelStockHandle(formulaInStockHandle.getId()).stream().map(DoctorWarehouseStockHandle::getId).collect(Collectors.toList());
//                if (formulaOutStockHandleIds.isEmpty())
//                    throw new InvalidException("formula.out.stock.handle.not.found", formulaInStockHandle.getId());
//
//                List<DoctorWarehouseMaterialHandle> formulaOutMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandles(formulaOutStockHandleIds);
//                if (formulaOutMaterialHandles.isEmpty())
//                    throw new InvalidException("formula.out.material.handle.not.found", formulaInStockHandle.getId());
//
//                BigDecimal totalFormulaOutAmount = formulaOutMaterialHandles
//                        .stream()
//                        .map(mh -> new BigDecimal(mh.getUnitPrice().toString()).multiply(mh.getQuantity()))
//                        .reduce((a, b) -> a.add(b))
//                        .orElse(new BigDecimal(0));
//
//                materialHandle.setUnitPrice(totalFormulaOutAmount.divide(materialHandle.getQuantity(), 4, BigDecimal.ROUND_HALF_UP));
//                materialHandle.setAmount(totalFormulaOutAmount.setScale(2,BigDecimal.ROUND_HALF_UP));

            } else if (materialHandle.getType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue())) {

                DoctorWarehouseMaterialHandle otherIn = settlementMaterialHandles.get(materialHandle.getRelMaterialHandleId());
                if (null == otherIn) {
                    throw new InvalidException("settlement.transfer.out.material.handle.not.found", materialHandle.getRelMaterialHandleId());
                }

                materialHandle.setUnitPrice(otherIn.getUnitPrice().setScale(4,BigDecimal.ROUND_HALF_UP));
                materialHandle.setAmount(materialHandle.getUnitPrice().multiply(materialHandle.getQuantity()).setScale(2,BigDecimal.ROUND_HALF_UP));
            }

            historyStockQuantity = historyStockQuantity.add(materialHandle.getQuantity());
            historyStockAmount = historyStockAmount.add(new BigDecimal(materialHandle.getUnitPrice().toString()).multiply(materialHandle.getQuantity()));
        } else {
            if (!materialHandle.getType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {
                //出库类型：领料出库，盘亏出库，调拨出库，配方生产出库
                log.debug("material handle:{},history amount:{},history quantity:{}", materialHandle.getId(), historyStockAmount, historyStockQuantity);
                if (historyStockAmount.compareTo(new BigDecimal("0")) < 0 || historyStockQuantity.compareTo(new BigDecimal("0")) <= 0) {
                    log.error("history amount or quantity is small then zero,can not settlement for material handle:{}", materialHandle.getId());
                    throw new InvalidException("settlement.history.quantity.amount.zero");
                }
            }
            if(materialHandle.getType().equals(WarehouseMaterialHandleType.FORMULA_OUT.getValue())){
                materialHandle.setAmount(historyStockAmount.multiply(materialHandle.getQuantity()).divide(historyStockQuantity, 2, BigDecimal.ROUND_HALF_UP));
                materialHandle.setUnitPrice(historyStockAmount.divide(historyStockQuantity, 4, BigDecimal.ROUND_HALF_UP));
                // 配方入库单据 （陈娟 2018-10-08）
                DoctorWarehouseMaterialHandle materialHandleIn = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());
                BigDecimal amout = materialHandleIn.getAmount().add(historyStockAmount.multiply(materialHandle.getQuantity()).divide(historyStockQuantity, 2, BigDecimal.ROUND_HALF_UP));
                doctorWarehouseMaterialHandleDao.updateUnitPriceAndAmountById(materialHandleIn.getId(), amout.divide(materialHandleIn.getQuantity()), amout);
            }else if (materialHandle.getType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {

                DoctorWarehouseMaterialHandle otherIn = settlementMaterialHandles.get(materialHandle.getRelMaterialHandleId());
                if (null == otherIn) {
                    //可能对于的出库单据明细已经结算
                    otherIn = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());
                    if (null == otherIn || otherIn.getDeleteFlag().equals(WarehouseMaterialHandleDeleteFlag.DELETE.getValue()))
                        throw new InvalidException("settlement.out.material.handle.not.found", materialHandle.getRelMaterialHandleId());
                }

                materialHandle.setUnitPrice(otherIn.getUnitPrice().setScale(4,BigDecimal.ROUND_HALF_UP));
                materialHandle.setAmount(materialHandle.getUnitPrice().multiply(materialHandle.getQuantity()).setScale(2,BigDecimal.ROUND_HALF_UP));
            }else{
                    materialHandle.setAmount(historyStockAmount.multiply(materialHandle.getQuantity()).divide(historyStockQuantity, 2, BigDecimal.ROUND_HALF_UP));
                    materialHandle.setUnitPrice(historyStockAmount.divide(historyStockQuantity, 4, BigDecimal.ROUND_HALF_UP));
            }
            if (materialHandle.getType().equals(WarehouseMaterialHandleType.OUT.getValue())) {
                doctorWarehouseMaterialApplyDao.updateUnitPriceAndAmountByMaterialHandle(materialHandle.getId(), materialHandle.getUnitPrice().setScale(4,BigDecimal.ROUND_HALF_UP), materialHandle.getQuantity().multiply(materialHandle.getUnitPrice()).setScale(2,BigDecimal.ROUND_HALF_UP));
            }

            historyStockQuantity = historyStockQuantity.subtract(materialHandle.getQuantity());
            historyStockAmount = historyStockAmount.subtract(materialHandle.getAmount());
        }

//        materialHandle.setAmount(materialHandle.getUnitPrice().multiply(materialHandle.getQuantity()));
//        doctorWarehouseMaterialHandleDao.update(materialHandle);
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
