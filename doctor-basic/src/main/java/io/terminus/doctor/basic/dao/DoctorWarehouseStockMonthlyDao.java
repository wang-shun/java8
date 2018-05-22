package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-29 13:22:37
 * Created by [ your name ]
 */
@Slf4j
@Repository
public class DoctorWarehouseStockMonthlyDao extends MyBatisDao<DoctorWarehouseStockMonthly> {


    @Deprecated
    public AmountAndQuantityDto statistics(Map<String, Object> params) {

        List<DoctorWarehouseStockMonthly> monthlies = this.getSqlSession().selectList(this.sqlId("statistics"), params);
        BigDecimal amount = new BigDecimal(0);
        BigDecimal quantity = new BigDecimal(0);
        for (DoctorWarehouseStockMonthly monthly : monthlies) {
            if (monthly.getBalanceAmount() != null) {
                amount = amount.add(monthly.getBalanceAmount());
                quantity = quantity.add(monthly.getBalanceQuantity());
            }
        }
        return new AmountAndQuantityDto(amount, quantity);
    }


    /**
     * 分组统计某一个仓库下sku的余额和余量
     *
     * @param warehouseId
     * @return
     */
    @Deprecated
    public Map<Long/*skuId*/, AmountAndQuantityDto> statisticsGroupBySku(Long warehouseId, Date handleDate) {

        Map<String, Object> params = new HashMap<>(2);
        params.put("handleDate", handleDate);
        params.put("warehouseId", warehouseId);

        List<DoctorWarehouseStockMonthly> monthlies = this.getSqlSession().selectList(this.sqlId("statisticsGroupBySku"), params);

        Map<Long, AmountAndQuantityDto> statistics = new HashMap<>();
        for (DoctorWarehouseStockMonthly monthly : monthlies) {
            statistics.put(monthly.getMaterialId(), new AmountAndQuantityDto(monthly.getBalanceAmount(), monthly.getBalanceQuantity()));
        }

        return statistics;
    }

    @Deprecated
    public AmountAndQuantityDto statistics(Long warehouseId, Date handleDate) {

        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("handleDate", handleDate);
        DoctorWarehouseStockMonthly monthly = this.getSqlSession().selectOne(this.sqlId("statisticsWarehouse"), params);
        if (null == monthly || null == monthly.getBalanceQuantity() || null == monthly.getBalanceAmount())
            return new AmountAndQuantityDto(new BigDecimal(0), new BigDecimal(0));

        return new AmountAndQuantityDto(monthly.getBalanceAmount(), monthly.getBalanceQuantity());
    }


    /**
     * 查询余额和余量
     *
     * @param warehouseId    仓库id
     * @param skuId          物料id
     * @param settlementDate 会计年月
     * @return
     */
    public DoctorWarehouseStockMonthly findBalanceBySettlementDate(Long warehouseId, Long skuId, Date settlementDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("skuId", skuId);
        params.put("settlementDate", settlementDate);

        return this.sqlSession.selectOne(this.sqlId("findBalanceBySettlementDate"), params);
    }

    /**
     * 查询每个仓库的余额和余量
     *
     * @return Key:warehouseId+'-'+materialId
     */
    public Map<String, AmountAndQuantityDto> findEachWarehouseBalanceBySettlementDate(Long orgId, Date settlementDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        params.put("settlementDate", settlementDate);

        List<Map<String, Object>> result = this.sqlSession.selectList(this.sqlId("findEachWarehouseBalanceBySettlementDate"), params);

        log.info("get balance for org {} and settlement date {}", orgId, settlementDate);

        Map<String, AmountAndQuantityDto> balances = new HashMap<>();

        result.forEach(r -> {
            Long warehouseId = (Long) r.get("warehouseId");
            Long materialId = (Long) r.get("materialId");
            BigDecimal amount = (BigDecimal) r.get("amount");
            BigDecimal quantity = (BigDecimal) r.get("quantity");

            log.info("warehouse:{},material:{},amount:{},quantity:{}", warehouseId, materialId, amount, quantity);

            String key = warehouseId + "-" + materialId;

            balances.put(key, new AmountAndQuantityDto(amount, quantity));
        });

        return balances;
    }

    public void reverseSettlement(Long orgId, Date settlementDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", orgId);
        params.put("handleDate", settlementDate);

        this.sqlSession.delete(this.sqlId("reverseSettlement"), params);
    }

    public List<Map> listByHouseIdTime(Map<String, Object> criteria) {

        return this.sqlSession.selectList("listByHouseIdTime", criteria);

    }

    public List<Map> monthWarehouseDetail(Map<String, Object> criteria) {

        return this.sqlSession.selectList("monthWarehouseDetail", criteria);

    }
}
