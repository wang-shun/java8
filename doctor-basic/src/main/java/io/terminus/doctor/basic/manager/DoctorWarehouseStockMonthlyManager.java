package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehouseStockMonthlyDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO 结算的时候需要统计
 * Created by sunbo@terminus.io on 2017/9/29.
 */
@Component
public class DoctorWarehouseStockMonthlyManager {

    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;

    public void count(Long warehouseId, Long materialId, int handleYear, int handleMonth, BigDecimal quantity, long unitPrice, boolean add) {

        List<DoctorWarehouseStockMonthly> stockMonthlies = doctorWarehouseStockMonthlyDao.list(DoctorWarehouseStockMonthly.builder()
                .warehouseId(warehouseId)
                .materialId(materialId)
//                .handleYear(handleYear)
//                .handleMonth(handleMonth)
                .build());

        BigDecimal amount = quantity.multiply(new BigDecimal(unitPrice));
        if (stockMonthlies.isEmpty()) {
            doctorWarehouseStockMonthlyDao.create(DoctorWarehouseStockMonthly.builder()
                    .warehouseId(warehouseId)
                    .materialId(materialId)
//                    .handleYear(handleYear)
//                    .handleMonth(handleMonth)
//                    .handleDate(DateUtil.toDate(handleYear + "-" + handleMonth + "-01"))
                    .balanceQuantity(add ? quantity : new BigDecimal(0).subtract(quantity))
                    .balanceAmount(add ? amount : new BigDecimal(0).subtract(amount))
                    .build());
        } else {
            DoctorWarehouseStockMonthly stockMonthly = stockMonthlies.get(0);
            if (add) {
                stockMonthly.setBalanceQuantity(stockMonthly.getBalanceQuantity().add(quantity));
                stockMonthly.setBalanceAmount(stockMonthly.getBalanceAmount().add(amount));
            } else {
                stockMonthly.setBalanceQuantity(stockMonthly.getBalanceQuantity().subtract(quantity));
                stockMonthly.setBalanceAmount(stockMonthly.getBalanceAmount().subtract(amount));
            }
            doctorWarehouseStockMonthlyDao.update(stockMonthly);
        }

    }
}
