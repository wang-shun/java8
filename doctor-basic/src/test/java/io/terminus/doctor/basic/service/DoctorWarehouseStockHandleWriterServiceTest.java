package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockHandleWriteService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by sunbo@terminus.io on 2017/12/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DoctorWarehouseStockWriteServiceTest.ServiceConfig.class)
@ActiveProfiles("test")
@Sql("classpath:stock-handle-delete.sql")
public class DoctorWarehouseStockHandleWriterServiceTest {

    @Autowired
    private DoctorWarehouseStockHandleWriteService doctorWarehouseStockHandleWriteService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;
    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;
    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;
    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;
    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;


    @Test
    public void testDeleteOnStockNotEnough() {

        Response r = doctorWarehouseStockHandleWriteService.delete(1L);
        Assert.assertEquals(false, r.isSuccess());
        Assert.assertEquals("一号大仓中稻草库存不足，现有10千克", r.getError());
    }

    @Test
    public void testDeleteOnTransfer() {
        Response r = doctorWarehouseStockHandleWriteService.delete(2L);
        System.out.println(r.getError());
        Assert.assertEquals("处理结果不匹配", true, r.isSuccess());
        DoctorWarehouseStock transferOutStock = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(87L, 33L).get();
        Assert.assertEquals(14, transferOutStock.getQuantity().doubleValue(), 2);
        Assert.assertEquals(0, doctorWarehousePurchaseDao.findById(1L).getHandleQuantity().doubleValue(), 2);
        Assert.assertEquals(27, doctorWarehousePurchaseDao.findById(2L).getHandleQuantity().doubleValue(), 2);
        DoctorWarehouseMaterialHandle transferOutHandle = doctorWarehouseMaterialHandleDao.findById(6L);
        Assert.assertEquals(WarehouseMaterialHandleDeleteFlag.DELETE.getValue(), transferOutHandle.getDeleteFlag().intValue());

        Assert.assertEquals(1, doctorWarehouseStockDao.findBySkuIdAndWarehouseId(87L, 34L).get().getQuantity().doubleValue(), 2);
        Assert.assertEquals(4, doctorWarehousePurchaseDao.findById(3L).getHandleQuantity().doubleValue(), 2);
        Assert.assertEquals(WarehouseMaterialHandleDeleteFlag.DELETE.getValue(), doctorWarehouseMaterialHandleDao.findById(7L).getDeleteFlag().intValue());

        Assert.assertEquals(4, doctorWarehouseStockMonthlyDao.list(DoctorWarehouseStockMonthly.builder()
                .warehouseId(33L).materialId(87L).handleYear(2017)
                .handleMonth(11)
                .build()).get(0).getBalanceQuantity().doubleValue(), 2);


        Assert.assertEquals(6, doctorWarehouseStockMonthlyDao.list(DoctorWarehouseStockMonthly.builder()
                .warehouseId(34L).materialId(87L).handleYear(2017)
                .handleMonth(11)
                .build()).get(0).getBalanceQuantity().doubleValue(), 2);
    }

    @Test
    public void testDeleteOnOut() {

        Response r = doctorWarehouseStockHandleWriteService.delete(3L);
        System.out.println(r.getError());
        Assert.assertEquals(true, r.isSuccess());

        DoctorWarehousePurchase purchase = doctorWarehousePurchaseDao.findById(4L);
        Assert.assertEquals(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue(), purchase.getHandleFinishFlag().intValue());
        Assert.assertEquals(null, doctorWarehouseMaterialApplyDao.findMaterialHandle(8L));
        Assert.assertEquals(WarehouseMaterialHandleDeleteFlag.DELETE.getValue(), doctorWarehouseMaterialHandleDao.findById(8L).getDeleteFlag().intValue());
        Assert.assertNull(doctorWarehouseStockHandleDao.findById(3L));
    }


}
