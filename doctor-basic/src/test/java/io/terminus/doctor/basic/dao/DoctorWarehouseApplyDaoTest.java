package io.terminus.doctor.basic.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * Created by sunbo@terminus.io on 2017/12/5.
 */
public class DoctorWarehouseApplyDaoTest extends BaseDaoTest {

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(statements = "insert into doctor_warehouse_material_apply(material_handle_id)values(1),(1)")
    public void testDeleteByMaterialHandle() {
        doctorWarehouseMaterialApplyDao.deleteByMaterialHandle(1L);
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "doctor_warehouse_material_apply"));

    }
}
