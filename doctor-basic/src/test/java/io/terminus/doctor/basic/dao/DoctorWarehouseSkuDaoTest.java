package io.terminus.doctor.basic.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * Created by sunbo@terminus.io on 2017/12/8.
 */
public class DoctorWarehouseSkuDaoTest extends BaseDaoTest {

    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;


    @Test
    @Sql(statements = "insert into doctor_warehouse_sku(id,org_id,type,code)values(1,1,1,'YL0210'),(2,1,1,'YL0304'),(3,1,1,'YL0200')")
    public void testFindLastCode() {
        Assert.assertEquals("YL0304", doctorWarehouseSkuDao.findLastCode(1L, 1));
    }

    @Test
    public void testFindLastCodeOnNoCode() {
        Assert.assertNull(doctorWarehouseSkuDao.findLastCode(1L, 2));
    }
}
