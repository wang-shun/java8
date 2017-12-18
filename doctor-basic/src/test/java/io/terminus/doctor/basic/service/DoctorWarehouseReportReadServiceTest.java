package io.terminus.doctor.basic.service;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseReportReadService;
import io.terminus.doctor.common.enums.WareHouseType;
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
 * Created by sunbo@terminus.io on 2017/12/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DoctorWarehouseStockWriteServiceTest.ServiceConfig.class)
@ActiveProfiles("test")
@Sql("classpath:warehouse.sql")
@Sql("classpath:stock-monthly-data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DoctorWarehouseReportReadServiceTest {


    @Autowired
    private DoctorWarehouseReportReadService doctorWarehouseReportReadService;


    /**
     * 测试某一个猪厂下，某一个物料的余额和余量
     */
    @Test
    public void testCountFarmBalance() {

        Response<AmountAndQuantityDto> balance = doctorWarehouseReportReadService.countFarmBalance(97L, 12L);
        Assert.assertEquals(2090700L, balance.getResult().getAmount());
        Assert.assertEquals(6925, balance.getResult().getQuantity().doubleValue(), 0);
    }


    @Test
    public void testCountBalanceEachWarehouseType() {
        Map<Integer, AmountAndQuantityDto> eachWarehouseTypeBalance = RespHelper.or500(doctorWarehouseReportReadService.countBalanceEachWarehouseType(97L));
        AmountAndQuantityDto feedBalance = eachWarehouseTypeBalance.get(WareHouseType.FEED.getKey());
        Assert.assertEquals(5435126L, feedBalance.getAmount());
        Assert.assertEquals(19454, feedBalance.getQuantity().doubleValue(), 0);
        AmountAndQuantityDto materialBalance = eachWarehouseTypeBalance.get(WareHouseType.MATERIAL.getKey());
        Assert.assertEquals(14687536L, materialBalance.getAmount());
        Assert.assertEquals(114696, materialBalance.getQuantity().doubleValue(), 0);
        AmountAndQuantityDto vaccBalance = eachWarehouseTypeBalance.get(WareHouseType.VACCINATION.getKey());
        Assert.assertEquals(1450000L, vaccBalance.getAmount());
        Assert.assertEquals(1090, vaccBalance.getQuantity().doubleValue(), 0);
        AmountAndQuantityDto mediBalance = eachWarehouseTypeBalance.get(WareHouseType.MEDICINE.getKey());
        Assert.assertEquals(4964105L, mediBalance.getAmount());
        Assert.assertEquals(9928.2, mediBalance.getQuantity().doubleValue(), 0);
        Assert.assertNull(eachWarehouseTypeBalance.get(WareHouseType.CONSUME.getKey()));
    }

    @Test
    public void testCountWarehouseBalance() {
        AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseReportReadService.countWarehouseBalance(221L));
        Assert.assertEquals(1515300L, balance.getAmount());
        Assert.assertEquals(6000, balance.getQuantity().doubleValue(), 0);
    }

    @Test
    public void testCountEachWarehouseBalance() {
        Map<Long, AmountAndQuantityDto> eachWarehouseBalance = RespHelper.or500(doctorWarehouseReportReadService.countEachWarehouseBalance(404L, 1));
        AmountAndQuantityDto balance1 = eachWarehouseBalance.get(282L);
        Assert.assertEquals(510800L, balance1.getAmount());
        Assert.assertEquals(1100, balance1.getQuantity().doubleValue(), 0);
        AmountAndQuantityDto balance2 = eachWarehouseBalance.get(218L);
        Assert.assertEquals(10106L, balance2.getAmount());
        Assert.assertEquals(31, balance2.getQuantity().doubleValue(), 0);
        AmountAndQuantityDto balance3 = eachWarehouseBalance.get(222L);
        Assert.assertEquals(1466415, balance3.getAmount());
        Assert.assertEquals(1215, balance3.getQuantity().doubleValue(), 0);
    }

    @Test
    public void testCountEachMaterialBalance() {
        Map<Long, AmountAndQuantityDto> eachSkuBalance = RespHelper.or500(doctorWarehouseReportReadService.countEachMaterialBalance(0L, 255L));
        AmountAndQuantityDto balance228 = eachSkuBalance.get(228L);
        Assert.assertEquals(655000L, balance228.getAmount());
        Assert.assertEquals(185, balance228.getQuantity().doubleValue(), 0);
        AmountAndQuantityDto balance227 = eachSkuBalance.get(227L);
        Assert.assertEquals(475000L, balance227.getAmount());
        Assert.assertEquals(95, balance227.getQuantity().doubleValue(), 0);
    }

    @Test
    public void testCountMaterialBalance() {

        AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseReportReadService.countMaterialBalance(230L, 70L));
        Assert.assertEquals(0L, balance.getAmount());
        Assert.assertEquals(0, balance.getQuantity().doubleValue(), 2);
    }

    @Test
    public void testCountBalance() {
        AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseReportReadService.countBalance(777L, 1, 220L, 30L, null));
        Assert.assertEquals(156000, balance.getAmount());
    }

    @Test
    public void testCountBalanceOnNoData() {
        AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseReportReadService.countBalance(777L, 1, 220L, 33L, null));
        Assert.assertEquals(0, balance.getAmount());
    }

    @Test
    public void testCountBalanceOnFarm() {
        AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseReportReadService.countBalance(777L, null, null, null, null));
        Assert.assertEquals(3317875, balance.getAmount());
    }


}
