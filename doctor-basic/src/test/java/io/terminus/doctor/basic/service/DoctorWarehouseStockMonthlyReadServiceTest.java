package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockMonthlyReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/12/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DoctorWarehouseStockWriteServiceTest.ServiceConfig.class)
@ActiveProfiles("test")
@Sql("classpath:stock-monthly-statistics.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DoctorWarehouseStockMonthlyReadServiceTest {

    @Autowired
    private DoctorWarehouseStockMonthlyReadService doctorWarehouseStockMonthlyReadService;


    @Test
    public void testCountWarehouseBalance() {
        AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countWarehouseBalance(1L, DateUtil.toYYYYMM("2017-07")));

        Assert.assertEquals(2812, balance.getQuantity().doubleValue(), 0);
        Assert.assertEquals(3516, balance.getAmount());
    }

    @Test
    public void testCountEachMaterialBalance() {
        Map<Long, AmountAndQuantityDto> balanceMap = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countEachMaterialBalance(1L, 2017, 7));

        Assert.assertEquals(2806, balanceMap.get(1L).getQuantity().doubleValue(), 0);
        Assert.assertEquals(3506, balanceMap.get(1L).getAmount());

        Assert.assertEquals(6, balanceMap.get(2L).getQuantity().doubleValue(), 0);
        Assert.assertEquals(10, balanceMap.get(2L).getAmount());
    }

    @Test
    public void testCountEachMaterialBalanceOnNoSku() {
        Map<Long, AmountAndQuantityDto> balanceMap = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countEachMaterialBalance(2L, 2017, 7));

        Assert.assertNull(balanceMap.get(1L));
        Assert.assertEquals(300, balanceMap.get(2L).getQuantity().doubleValue(), 0);
        Assert.assertEquals(100, balanceMap.get(2L).getAmount());
    }

    @Test
    public void testCountMaterialBalance() {
        AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countMaterialBalance(1L, 1L, 2017, 7));
        Assert.assertEquals(2806, balance.getQuantity().doubleValue(), 0);
        Assert.assertEquals(3506, balance.getAmount());
    }
}
