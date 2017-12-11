package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
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

    @Test
    @Sql(statements = "insert into doctor_warehouse_material_apply(material_handle_id,pig_group_id)values(1,null),(1,2)")
    @Sql(statements = "delete from doctor_warehouse_material_apply where material_handle_id=1", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testFindByMaterialHandle() {
        DoctorWarehouseMaterialApply apply = doctorWarehouseMaterialApplyDao.findMaterialHandle(1L);
        Assert.assertEquals(2L, apply.getPigGroupId().longValue());
    }


    @Test
    @Sql(statements = "insert into doctor_warehouse_material_apply(material_handle_id,pig_group_id)values(2,null),(2,9)")
    @Sql(statements = "delete from doctor_warehouse_material_apply where material_handle_id=2", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testFindByMaterialHandleOnNoData() {
        DoctorWarehouseMaterialApply apply = doctorWarehouseMaterialApplyDao.findMaterialHandle(1L);
        Assert.assertNull(apply);
    }

    @Test
    @Sql(statements = "insert into doctor_warehouse_material_apply(material_handle_id,pig_group_id)values(2,null)")
    @Sql(statements = "delete from doctor_warehouse_material_apply where material_handle_id=2", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testFindByMaterialHandleOnBarn() {
        DoctorWarehouseMaterialApply apply = doctorWarehouseMaterialApplyDao.findMaterialHandle(2L);
        Assert.assertNotNull(apply);
        Assert.assertNull(apply.getPigGroupId());
    }

}
