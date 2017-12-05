package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-29 13:22:37
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseStockMonthlyDao extends MyBatisDao<DoctorWarehouseStockMonthly> {


    public AmountAndQuantityDto statistics(Map<String, Object> params) {

        List<DoctorWarehouseStockMonthly> monthlies = this.getSqlSession().selectList(this.sqlId("statistics"), params);
        long amount = 0;
        BigDecimal quantity = new BigDecimal(0);
        for (DoctorWarehouseStockMonthly monthly : monthlies) {
            amount += monthly.getBalacneAmount();
            quantity = quantity.add(monthly.getBalanceQuantity());
        }
        return new AmountAndQuantityDto(amount, quantity);
    }


    /**
     * 分组统计某一个仓库下sku的余额和余量
     *
     * @param warehouseId
     * @return
     */
    public Map<Long/*skuId*/, AmountAndQuantityDto> statisticsGroupBySku(Long warehouseId) {

        List<DoctorWarehouseStockMonthly> monthlies = this.getSqlSession().selectList(this.sqlId("statisticsGroupBySku"), Collections.singletonMap("warehouseId", warehouseId));

        Map<Long, AmountAndQuantityDto> statistics = new HashMap<>();
        for (DoctorWarehouseStockMonthly monthly : monthlies) {
            statistics.put(monthly.getMaterialId(), new AmountAndQuantityDto(monthly.getBalacneAmount(), monthly.getBalanceQuantity()));
        }

        return statistics;
    }

}
