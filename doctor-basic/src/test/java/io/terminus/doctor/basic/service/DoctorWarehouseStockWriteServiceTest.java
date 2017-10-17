package io.terminus.doctor.basic.service;

import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.basic.base.BaseServiceTest;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.manager.MaterialInWareHouseManager;
import io.terminus.doctor.basic.model.warehouseV2.*;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.statements.SpringRepeat;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

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
    DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;
    @Autowired
    DoctorMaterialVendorDao doctorMaterialVendorDao;
    @Autowired
    DoctorMaterialCodeDao doctorMaterialCodeDao;

    @Test
    @Transactional
    public void testIn() {

        doctorWarehouseStockDao.create(DoctorWarehouseStock.builder()
                .farmId(407L)
                .warehouseId(190L)
                .skuId(157L)
                .quantity(new BigDecimal(10))
//                .unit("个")
                .build());

        WarehouseStockInDto inDto = new WarehouseStockInDto();
        inDto.setFarmId(407L);
        inDto.setWarehouseId(190L);
        inDto.setHandleDate(Calendar.getInstance());
        inDto.setOperatorId(592L);
        inDto.setOperatorName("许烧毁");

        WarehouseStockInDto.WarehouseStockInDetailDto detail = new WarehouseStockInDto.WarehouseStockInDetailDto();
        detail.setMaterialId(153L);
        detail.setQuantity(new BigDecimal(15));
//        detail.setUnit("个");
        detail.setUnitPrice(78990L);

        WarehouseStockInDto.WarehouseStockInDetailDto detail2 = new WarehouseStockInDto.WarehouseStockInDetailDto();
        detail2.setMaterialId(157L);
        detail2.setQuantity(new BigDecimal(10));
//        detail2.setUnit("件");
        detail2.setUnitPrice(32810L);

        List<WarehouseStockInDto.WarehouseStockInDetailDto> detailDtos = new ArrayList<>(2);
        detailDtos.add(detail);
        detailDtos.add(detail2);
        inDto.setDetails(detailDtos);
        doctorWarehouseStockWriteService.in(inDto);


        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                .skuId(153L)
                .build());
        Assert.assertEquals(1, stocks.size());
        Assert.assertEquals(153, stocks.get(0).getSkuId().longValue());
        Assert.assertEquals(15, stocks.get(0).getQuantity().intValue());

        stocks = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                .skuId(157L)
                .build());
        Assert.assertEquals(1, stocks.size());
        Assert.assertEquals(157, stocks.get(0).getSkuId().longValue());
        Assert.assertEquals(20, stocks.get(0).getQuantity().intValue());

        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .materialId(153L)
                .build());
        Assert.assertEquals(1, purchases.size());
        Assert.assertEquals(15, purchases.get(0).getQuantity().intValue());
        Assert.assertEquals(0, purchases.get(0).getHandleQuantity().intValue());
        Assert.assertEquals(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue(), purchases.get(0).getHandleFinishFlag().intValue());
        Assert.assertEquals(78990, purchases.get(0).getUnitPrice().longValue());

        purchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .materialId(157L)
                .build());
        Assert.assertEquals(1, purchases.size());
        Assert.assertEquals(10, purchases.get(0).getQuantity().intValue());
        Assert.assertEquals(32810, purchases.get(0).getUnitPrice().longValue());

        List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.list(DoctorWarehouseMaterialHandle.builder()
                .materialId(153L)
                .build());
        Assert.assertEquals(1, handles.size());
        Assert.assertEquals(15, handles.get(0).getQuantity().intValue());
        Assert.assertEquals(WarehouseMaterialHandleType.IN.getValue(), handles.get(0).getType().intValue());
        Assert.assertEquals(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue(), handles.get(0).getDeleteFlag().intValue());

        handles = doctorWarehouseMaterialHandleDao.list(DoctorWarehouseMaterialHandle.builder()
                .materialId(157L)
                .build());
        Assert.assertEquals(1, handles.size());
        Assert.assertEquals(10, handles.get(0).getQuantity().intValue());

        List<DoctorWarehouseStockHandle> stockHandles = doctorWarehouseStockHandleDao.list(Collections.emptyMap());
        Assert.assertEquals(1, stockHandles.size());
        Assert.assertNotNull(stockHandles.get(0).getSerialNo());
    }

    @Test
    @Transactional
    public void testMaterialCode() {
        WarehouseStockInDto inDto = new WarehouseStockInDto();
        inDto.setFarmId(407L);
        inDto.setWarehouseId(190L);
        inDto.setHandleDate(Calendar.getInstance());
        inDto.setOperatorId(592L);
        inDto.setOperatorName("许烧毁");

        WarehouseStockInDto.WarehouseStockInDetailDto detail = new WarehouseStockInDto.WarehouseStockInDetailDto();
        detail.setMaterialId(153L);
        detail.setQuantity(new BigDecimal(15));
//        detail.setUnit("个");
        detail.setUnitPrice(78990L);
//        detail.setVendorName("白金");
//        detail.setSpecification("899个/包");
//        detail.setMaterialCode("9998847700038775");
        inDto.setDetails(Collections.singletonList(detail));
        doctorWarehouseStockWriteService.in(inDto);

        List<DoctorMaterialVendor> vendors = doctorMaterialVendorDao.list(Collections.emptyMap());
        Assert.assertEquals(1, vendors.size());
        Assert.assertEquals(190L, vendors.get(0).getWarehouseId().longValue());
        Assert.assertEquals(153L, vendors.get(0).getMaterialId().longValue());
        Assert.assertEquals("白金", vendors.get(0).getVendorName());

        List<DoctorMaterialCode> codes = doctorMaterialCodeDao.list(Collections.emptyMap());
        Assert.assertEquals(1, codes.size());
        Assert.assertEquals(190L, codes.get(0).getWarehouseId().longValue());
        Assert.assertEquals(153L, codes.get(0).getMaterialId().longValue());
        Assert.assertEquals("899个/包", codes.get(0).getSpecification());
        Assert.assertEquals("9998847700038775", codes.get(0).getCode());
    }


    @Configuration
    @EnableAutoConfiguration(exclude = {DubboBaseAutoConfiguration.class})
    @ComponentScan(
            value = {
                    "io.terminus.doctor.basic.dao",
                    "io.terminus.doctor.basic.manager",
                    "io.terminus.doctor.basic.service",
                    "io.terminus.doctor.basic.cache", "io.terminus.doctor.basic.handler"})
    @AutoConfigureAfter(MybatisAutoConfiguration.class)
    @ImportAutoConfiguration(DoctorBasicConfiguration.class)
    public static class ServiceConfig {


        @Bean
        public CoreEventDispatcher coreEventDispatcher() {
            return new CoreEventDispatcher();
        }
    }

}
