package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.manager.DoctorWarehouseMaterialHandleManager;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by sunbo@terminus.io on 2017/12/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DoctorWarehouseStockWriteServiceTest.ServiceConfig.class)
@ActiveProfiles("test")
@Sql("classpath:material-handle-delete.sql")
public class DoctorWarehouseMaterialHandleManagerTest {

    @Autowired
    private DoctorWarehouseMaterialHandleManager doctorWarehouseMaterialHandleManager;
    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Test
    public void testDeleteOnIn() {
        DoctorWarehouseMaterialHandle materialHandle = doctorWarehouseMaterialHandleDao.findById(1L);
        doctorWarehouseMaterialHandleManager.delete(materialHandle);

        Assert.assertEquals(WarehouseMaterialHandleDeleteFlag.DELETE.getValue(), materialHandle.getDeleteFlag().intValue());

        Assert.assertNull(doctorWarehousePurchaseDao.findById(1L));

        DoctorWarehouseStock stock = doctorWarehouseStockDao.findById(15L);
        Assert.assertEquals(5, stock.getQuantity().doubleValue(), 2);
    }
}
