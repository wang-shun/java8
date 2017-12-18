package io.terminus.doctor.basic.service;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.manager.DoctorWarehousePurchaseManager;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.common.exception.InvalidException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by sunbo@terminus.io on 2017/12/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DoctorWarehouseStockWriteServiceTest.ServiceConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DoctorWarehousePurchaseManagerTest {

    @Autowired
    private DoctorWarehousePurchaseManager doctorWarehousePurchaseManager;
    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Test
    @Sql(statements = "insert into doctor_warehouse_handle_detail(id,material_handle_id,material_purchase_id)values(888,888,888)")
    @Sql(statements = "insert into doctor_warehouse_purchase(id,handle_quantity)values(888,0)")
    public void testDeletePurchase() {
        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setId(888L);
        materialHandle.setType(1);
        doctorWarehousePurchaseManager.delete(materialHandle);

        Assert.assertNull(doctorWarehousePurchaseDao.findById(888L));
    }

    @Test
    public void testDeletePurchaseOnNoRel() {
        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setId(889L);
        materialHandle.setType(1);
        doctorWarehousePurchaseManager.delete(materialHandle);
    }

    @Test
    @Sql(statements = "insert into doctor_warehouse_handle_detail(id,material_handle_id,material_purchase_id)values(899,899,899)")
    public void testDeletePurchaseOnNoPurchase() {
        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setId(899L);
        materialHandle.setType(1);
        doctorWarehousePurchaseManager.delete(materialHandle);
    }

    @Test(expected = InvalidException.class)
    @Sql(statements = "insert into doctor_warehouse_handle_detail(id,material_handle_id,material_purchase_id)values(898,898,898)")
    @Sql(statements = "insert into doctor_warehouse_purchase(id,handle_quantity)values(898,3)")
    public void testDeletePurchaseOnConsumed() {
        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setId(898L);
        materialHandle.setType(1);
        doctorWarehousePurchaseManager.delete(materialHandle);
    }

    @Test(expected = ServiceException.class)
    public void testDeletePurchaseOnOutMaterialHandle() {
        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setType(2);
        doctorWarehousePurchaseManager.delete(materialHandle);
    }
}
