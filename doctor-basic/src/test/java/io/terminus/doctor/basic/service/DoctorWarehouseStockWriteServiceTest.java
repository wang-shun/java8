package io.terminus.doctor.basic.service;

import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.basic.base.BaseServiceTest;
import io.terminus.doctor.basic.dao.DoctorWarehouseHandleDetailDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.manager.MaterialInWareHouseManager;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteServiceImpl;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.statements.SpringRepeat;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/9/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DoctorWarehouseStockWriteServiceTest.ServiceConfig.class)
@ActiveProfiles("test")
public class DoctorWarehouseStockWriteServiceTest {


    @Autowired
    DoctorWarehouseStockWriteService doctorWarehouseStockWriteService;

    @Autowired
    DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Autowired
    DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    DoctorWarehouseHandleDetailDao doctorWarehouseHandleDetailDao;

    @Autowired
    DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void testIn() {
        WarehouseStockInDto inDto = new WarehouseStockInDto();


        inDto.setFarmId(407L);
        inDto.setWarehouseId(190L);
        inDto.setHandleDate(new Date());
        inDto.setOperatorId(592L);
        inDto.setOperatorName("许烧毁");

        WarehouseStockInDto.WarehouseStockInDetailDto detail = new WarehouseStockInDto.WarehouseStockInDetailDto();
        detail.setMaterialId(153L);
        detail.setQuantity(new BigDecimal(15));
        detail.setUnit("个");
        detail.setUnitPrice(78990L);
        inDto.setDetails(Collections.singletonList(detail));
        doctorWarehouseStockWriteService.in(inDto);


        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(Collections.emptyMap());
        Assert.assertEquals(1, stocks.size());
        Assert.assertEquals(15, stocks.get(0).getQuantity().intValue());

        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(Collections.emptyMap());
        Assert.assertEquals(1, purchases.size());
        Assert.assertEquals(15, purchases.get(0).getQuantity().intValue());
        Assert.assertEquals(0, purchases.get(0).getHandleQuantity().intValue());
        Assert.assertEquals(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue(), purchases.get(0).getHandleFinishFlag().intValue());
        Assert.assertEquals(78990, purchases.get(0).getUnitPrice().longValue());
    }


    @Configuration
    @EnableAutoConfiguration(exclude = {DubboBaseAutoConfiguration.class})
    @ComponentScan(
            value = {
                    "io.terminus.doctor.basic.dao",
                    "io.terminus.doctor.basic.manager",
                    "io.terminus.doctor.basic.service",
                    "io.terminus.doctor.basic.cache","io.terminus.doctor.basic.handler"})
    @AutoConfigureAfter(MybatisAutoConfiguration.class)
    @ImportAutoConfiguration(DoctorBasicConfiguration.class)
    public static class ServiceConfig {


        @Bean
        public CoreEventDispatcher coreEventDispatcher(){
            return new CoreEventDispatcher();
        }
    }

}
